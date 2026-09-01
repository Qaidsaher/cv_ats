package com.example.core.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import com.example.domain.model.Resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat

class PrintPdfAdapter(private val pdfFile: File) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(pdfFile.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            FileInputStream(pdfFile).use { input ->
                FileOutputStream(destination?.fileDescriptor).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } >= 0) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onWriteCancelled()
                            return
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        }
    }
}

object PdfExportManager {

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "${pdfFile.nameWithoutExtension} - Resume")
                putExtra(Intent.EXTRA_TEXT, "Here is my resume attached in PDF format.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Resume PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "Open Resume PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun printPdf(context: Context, pdfFile: File) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val jobName = "${pdfFile.nameWithoutExtension} Resume Print"
            val printAttributes = PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            printManager?.print(jobName, PrintPdfAdapter(pdfFile), printAttributes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun savePdfToUri(context: Context, sourceFile: File, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getProfessionalFileName(resume: Resume): String {
        val rawName = resume.personalInfo.fullName.trim()
            .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_\\s-]"), "")
            .replace(Regex("\\s+"), "_")
        val cleanName = if (rawName.isBlank() || rawName.equals("Untitled", ignoreCase = true)) "Resume" else rawName

        val rawTitle = resume.personalInfo.professionalTitle.trim()
            .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_\\s-]"), "")
            .replace(Regex("\\s+"), "_")

        return if (rawTitle.isNotBlank()) {
            "${cleanName}_${rawTitle}_Resume.pdf"
        } else {
            "${cleanName}_Resume.pdf"
        }
    }

    fun getFileSizeFormatted(file: File): String {
        if (!file.exists()) return "0 KB"
        val bytes = file.length()
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) {
            return "${DecimalFormat("#.#").format(kb)} KB"
        }
        val mb = kb / 1024.0
        return "${DecimalFormat("#.##").format(mb)} MB"
    }
}

