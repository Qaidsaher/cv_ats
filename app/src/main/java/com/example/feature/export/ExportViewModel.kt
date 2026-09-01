package com.example.feature.export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.billing.BillingManager
import com.example.core.pdf.LocalPdfRenderer
import com.example.core.pdf.PdfExportManager
import com.example.data.repository.ResumeRepository
import com.example.data.repository.TemplateRepository
import com.example.domain.model.Resume
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ExportUiState(
    val resume: Resume? = null,
    val template: TemplateSpec? = null,
    val isGenerating: Boolean = false,
    val generatedPdfFile: File? = null,
    val fileSizeFormatted: String = "",
    val customFileName: String = "",
    val selectedPageFormat: String = "A4",
    val isPro: Boolean = false,
    val includeWatermark: Boolean = false,
    val errorMessage: String? = null,
    val exportSuccessMessage: String? = null
)

class ExportViewModel(
    savedStateHandle: SavedStateHandle,
    private val resumeRepository: ResumeRepository,
    private val templateRepository: TemplateRepository,
    private val billingManager: BillingManager,
    private val context: Context
) : ViewModel() {

    private val resumeId: String = checkNotNull(savedStateHandle["resumeId"])
    private val pdfRenderer = LocalPdfRenderer(context)

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val resume = resumeRepository.getResumeByIdDirect(resumeId)
            val isPro = billingManager.isProUser.first()

            if (resume != null) {
                val template = templateRepository.getTemplateById(resume.templateId)
                    ?: templateRepository.getTemplateById("ats_classic")
                    ?: TemplateSpec(id = "default", name = "Default")

                val defaultFileName = PdfExportManager.getProfessionalFileName(resume)
                val format = resume.customization.pageFormat.ifBlank { "A4" }

                _uiState.update {
                    it.copy(
                        resume = resume,
                        template = template,
                        customFileName = defaultFileName,
                        selectedPageFormat = format,
                        isPro = isPro,
                        includeWatermark = !isPro
                    )
                }

                generatePdfFile(resume, template, defaultFileName)
            } else {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Resume not found"
                    )
                }
            }
        }
    }

    fun updateFileName(name: String) {
        val sanitized = if (name.endsWith(".pdf", ignoreCase = true)) name else "$name.pdf"
        _uiState.update { it.copy(customFileName = sanitized) }
    }

    fun updatePageFormat(format: String) {
        _uiState.update { it.copy(selectedPageFormat = format) }
        val resume = _uiState.value.resume ?: return
        val updatedResume = resume.copy(
            customization = resume.customization.copy(pageFormat = format)
        )
        _uiState.update { it.copy(resume = updatedResume) }
        viewModelScope.launch {
            resumeRepository.saveResume(updatedResume)
            val template = _uiState.value.template ?: return@launch
            generatePdfFile(updatedResume, template, _uiState.value.customFileName)
        }
    }

    fun toggleWatermark(include: Boolean) {
        if (!_uiState.value.isPro && !include) {
            // Free users cannot remove watermark
            return
        }
        _uiState.update { it.copy(includeWatermark = include) }
    }

    fun regeneratePdf() {
        val resume = _uiState.value.resume ?: return
        val template = _uiState.value.template ?: return
        generatePdfFile(resume, template, _uiState.value.customFileName)
    }

    private fun generatePdfFile(resume: Resume, template: TemplateSpec, fileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            try {
                val cacheDir = File(context.cacheDir, "resumes").apply { mkdirs() }
                val targetFile = File(cacheDir, fileName)

                val result = pdfRenderer.generatePdf(resume, template, targetFile)
                if (result.isSuccess) {
                    val file = result.getOrThrow()
                    val sizeFormatted = PdfExportManager.getFileSizeFormatted(file)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatedPdfFile = file,
                            fileSizeFormatted = sizeFormatted
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "PDF Generation failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = e.localizedMessage ?: "Unexpected error during PDF generation"
                    )
                }
            }
        }
    }

    fun saveToUri(destinationUri: Uri) {
        val source = _uiState.value.generatedPdfFile ?: return
        viewModelScope.launch {
            val success = PdfExportManager.savePdfToUri(context, source, destinationUri)
            if (success) {
                _uiState.update { it.copy(exportSuccessMessage = "PDF successfully saved to device!") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to save PDF to selected location.") }
            }
        }
    }

    fun sharePdf() {
        val file = _uiState.value.generatedPdfFile ?: return
        PdfExportManager.sharePdf(context, file)
    }

    fun printPdf() {
        val file = _uiState.value.generatedPdfFile ?: return
        PdfExportManager.printPdf(context, file)
    }

    fun openPdf() {
        val file = _uiState.value.generatedPdfFile ?: return
        PdfExportManager.openPdf(context, file)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, exportSuccessMessage = null) }
    }
}
