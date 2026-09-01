package com.example.feature.preview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.pdf.LocalPdfRenderer
import com.example.core.pdf.PdfExportManager
import com.example.data.repository.ResumeRepository
import com.example.data.repository.TemplateRepository
import com.example.domain.model.Resume
import com.example.domain.model.TemplateCustomization
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PreviewUiState(
    val resume: Resume? = null,
    val template: TemplateSpec? = null,
    val allTemplates: List<TemplateSpec> = emptyList(),
    val isExporting: Boolean = false,
    val exportedPdfFile: File? = null,
    val exportSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class PreviewViewModel(
    private val context: Context,
    private val resumeRepository: ResumeRepository,
    private val templateRepository: TemplateRepository,
    private val resumeId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    private val pdfRenderer = LocalPdfRenderer(context)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val resume = resumeRepository.getResumeByIdDirect(resumeId)
            if (resume != null) {
                val template = templateRepository.getTemplateById(resume.templateId)
                _uiState.value = _uiState.value.copy(
                    resume = resume,
                    template = template
                )
            }
            templateRepository.getAllTemplates().collect { list ->
                _uiState.value = _uiState.value.copy(allTemplates = list)
            }
        }
    }

    fun changeTemplate(templateId: String) {
        val currentResume = _uiState.value.resume ?: return
        viewModelScope.launch {
            val newTemplate = templateRepository.getTemplateById(templateId) ?: return@launch
            val updatedResume = currentResume.copy(templateId = templateId)
            resumeRepository.saveResume(updatedResume)
            _uiState.value = _uiState.value.copy(
                resume = updatedResume,
                template = newTemplate
            )
        }
    }

    fun updateCustomization(customization: TemplateCustomization) {
        val currentResume = _uiState.value.resume ?: return
        viewModelScope.launch {
            val updatedResume = currentResume.copy(customization = customization)
            resumeRepository.saveResume(updatedResume)
            _uiState.value = _uiState.value.copy(resume = updatedResume)
        }
    }

    fun generatePdf(onReady: (File) -> Unit) {
        val resume = _uiState.value.resume ?: return
        val template = _uiState.value.template ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val fileName = PdfExportManager.getProfessionalFileName(resume)
            val cacheDir = File(context.cacheDir, "resumes")
            cacheDir.mkdirs()
            val outputFile = File(cacheDir, fileName)

            val result = pdfRenderer.generatePdf(resume, template, outputFile)
            if (result.isSuccess) {
                val file = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedPdfFile = file,
                    exportSuccessMessage = "PDF generated successfully!"
                )
                onReady(file)
            } else {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "PDF generation failed"
                )
            }
        }
    }
}
