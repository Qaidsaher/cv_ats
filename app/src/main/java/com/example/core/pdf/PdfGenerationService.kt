package com.example.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.domain.model.Resume
import com.example.domain.model.TemplateSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

interface ResumeRenderer {
    suspend fun generatePdf(
        resume: Resume,
        template: TemplateSpec,
        outputFile: File
    ): Result<File>
}

class LocalPdfRenderer(private val context: Context) : ResumeRenderer {

    // Standard Page Dimensions in PostScript Points (72 pt = 1 inch)
    // A4: 595.28 x 841.89 pt
    // Letter: 612 x 792 pt
    companion object {
        const val A4_WIDTH = 595
        const val A4_HEIGHT = 842
        const val LETTER_WIDTH = 612
        const val LETTER_HEIGHT = 792
    }

    override suspend fun generatePdf(
        resume: Resume,
        template: TemplateSpec,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val isLetter = resume.customization.pageFormat.equals("LETTER", ignoreCase = true)
            val pageWidth = if (isLetter) LETTER_WIDTH else A4_WIDTH
            val pageHeight = if (isLetter) LETTER_HEIGHT else A4_HEIGHT

            val pdfDocument = PdfDocument()

            // Resolve dynamic customization colors
            val primaryColorInt = parseColorSafely(
                resume.customization.primaryColorHex ?: template.colors.primary,
                Color.parseColor("#1F6F5C")
            )
            val secondaryColorInt = parseColorSafely(
                resume.customization.secondaryColorHex ?: template.colors.secondary,
                Color.parseColor("#444444")
            )
            val accentColorInt = parseColorSafely(
                resume.customization.accentColorHex ?: template.colors.accent,
                Color.parseColor("#E0A96D")
            )
            val backgroundColorInt = parseColorSafely(template.colors.background, Color.WHITE)
            val sidebarColorInt = parseColorSafely(template.colors.sidebar, Color.parseColor("#F4F7F6"))
            val textPrimaryColorInt = parseColorSafely(template.colors.textPrimary, Color.parseColor("#111111"))
            val textSecondaryColorInt = parseColorSafely(template.colors.textSecondary, Color.parseColor("#444444"))

            val isRtl = resume.isRtl

            // Calculate margins
            val marginTop = template.page.marginTop
            val marginBottom = template.page.marginBottom
            val marginStart = template.page.marginStart
            val marginEnd = template.page.marginEnd

            // Render single or multi-page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // 1. Draw Page Background
            val bgPaint = Paint().apply {
                color = backgroundColorInt
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // 2. Draw Layout Specific Structure
            val layoutType = template.layout.type
            val sidebarWidth = (pageWidth * template.layout.sidebarWidth).toFloat()

            when (layoutType) {
                "sidebar_start" -> {
                    // Sidebar background on start side (Left for LTR, Right for RTL)
                    val sidebarPaint = Paint().apply {
                        color = sidebarColorInt
                        style = Paint.Style.FILL
                    }
                    if (isRtl) {
                        canvas.drawRect(pageWidth - sidebarWidth, 0f, pageWidth.toFloat(), pageHeight.toFloat(), sidebarPaint)
                    } else {
                        canvas.drawRect(0f, 0f, sidebarWidth, pageHeight.toFloat(), sidebarPaint)
                    }
                    renderSidebarLayout(
                        canvas, resume, template, pageWidth, pageHeight,
                        sidebarWidth, isRtl, marginTop, marginBottom, marginStart, marginEnd,
                        primaryColorInt, secondaryColorInt, accentColorInt,
                        textPrimaryColorInt, textSecondaryColorInt
                    )
                }
                "sidebar_end" -> {
                    val sidebarPaint = Paint().apply {
                        color = sidebarColorInt
                        style = Paint.Style.FILL
                    }
                    if (isRtl) {
                        canvas.drawRect(0f, 0f, sidebarWidth, pageHeight.toFloat(), sidebarPaint)
                    } else {
                        canvas.drawRect(pageWidth - sidebarWidth, 0f, pageWidth.toFloat(), pageHeight.toFloat(), sidebarPaint)
                    }
                    renderSidebarLayout(
                        canvas, resume, template, pageWidth, pageHeight,
                        sidebarWidth, !isRtl, marginTop, marginBottom, marginStart, marginEnd,
                        primaryColorInt, secondaryColorInt, accentColorInt,
                        textPrimaryColorInt, textSecondaryColorInt
                    )
                }
                "header_split" -> {
                    // Dark top banner
                    val bannerHeight = 110f
                    val bannerPaint = Paint().apply {
                        color = primaryColorInt
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), bannerHeight, bannerPaint)

                    renderHeaderSplitLayout(
                        canvas, resume, template, pageWidth, pageHeight,
                        bannerHeight, isRtl, marginTop, marginBottom, marginStart, marginEnd,
                        primaryColorInt, secondaryColorInt, accentColorInt,
                        textPrimaryColorInt, textSecondaryColorInt
                    )
                }
                else -> {
                    // Single column / standard flow
                    renderSingleColumnLayout(
                        canvas, resume, template, pageWidth, pageHeight,
                        isRtl, marginTop, marginBottom, marginStart, marginEnd,
                        primaryColorInt, secondaryColorInt, accentColorInt,
                        textPrimaryColorInt, textSecondaryColorInt
                    )
                }
            }

            pdfDocument.finishPage(page)

            // Write to output file
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun renderSingleColumnLayout(
        canvas: Canvas,
        resume: Resume,
        template: TemplateSpec,
        pageWidth: Int,
        pageHeight: Int,
        isRtl: Boolean,
        marginTop: Float,
        marginBottom: Float,
        marginStart: Float,
        marginEnd: Float,
        primaryColor: Int,
        secondaryColor: Int,
        accentColor: Int,
        textPrimaryColor: Int,
        textSecondaryColor: Int
    ) {
        var currentY = marginTop + 10f
        val contentWidth = pageWidth - marginStart - marginEnd
        val textAlignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL

        // 1. Header (Name, Title, Contact Info)
        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = template.typography.nameSize * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = template.typography.titleSize * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val contactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textSecondaryColor
            textSize = template.typography.captionSize * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimaryColor
            textSize = template.typography.bodySize * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        // Draw Name
        val fullName = resume.personalInfo.fullName
        val nameLayout = createStaticLayout(fullName, namePaint, contentWidth.toInt(), textAlignment)
        canvas.save()
        canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
        nameLayout.draw(canvas)
        canvas.restore()
        currentY += nameLayout.height + 4f

        // Draw Professional Title
        if (resume.personalInfo.professionalTitle.isNotBlank()) {
            val titleLayout = createStaticLayout(resume.personalInfo.professionalTitle, titlePaint, contentWidth.toInt(), textAlignment)
            canvas.save()
            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
            titleLayout.draw(canvas)
            canvas.restore()
            currentY += titleLayout.height + 6f
        }

        // Contact info line
        val contacts = listOf(
            resume.personalInfo.email,
            resume.personalInfo.phone,
            resume.personalInfo.locationFormatted,
            resume.personalInfo.linkedIn,
            resume.personalInfo.gitHub
        ).filter { it.isNotBlank() }.joinToString("  •  ")

        if (contacts.isNotBlank()) {
            val contactLayout = createStaticLayout(contacts, contactPaint, contentWidth.toInt(), textAlignment)
            canvas.save()
            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
            contactLayout.draw(canvas)
            canvas.restore()
            currentY += contactLayout.height + 12f
        }

        // Section Divider Under Header
        val dividerPaint = Paint().apply {
            color = primaryColor
            strokeWidth = 1.5f
        }
        canvas.drawLine(marginStart, currentY, pageWidth - marginEnd, currentY, dividerPaint)
        currentY += 12f

        // 2. Render Sections based on sectionOrder
        for (sectionKey in resume.sectionOrder) {
            if (resume.sectionVisibility[sectionKey] == false) continue

            when (sectionKey) {
                "summary" -> {
                    if (resume.summary.isNotBlank()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "الملخص المهني" else "Professional Summary", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        val summaryLayout = createStaticLayout(resume.summary, bodyPaint, contentWidth.toInt(), textAlignment)
                        canvas.save()
                        canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                        summaryLayout.draw(canvas)
                        canvas.restore()
                        currentY += summaryLayout.height + 10f
                    }
                }
                "experience" -> {
                    if (resume.experiences.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "الخبرات المهنية" else "Experience", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        for (exp in resume.experiences) {
                            val roleCompany = "${exp.jobTitle} - ${exp.company}"
                            val dateLoc = "${exp.startDate} – ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate} ${if (exp.location.isNotBlank()) " | ${exp.location}" else ""}"

                            val rolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textPrimaryColor
                                textSize = 10.5f * resume.customization.fontSizeScale
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }
                            val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textSecondaryColor
                                textSize = 8.5f * resume.customization.fontSizeScale
                                typeface = Typeface.DEFAULT
                            }

                            val roleLayout = createStaticLayout(roleCompany, rolePaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            roleLayout.draw(canvas)
                            canvas.restore()
                            currentY += roleLayout.height + 2f

                            val dateLayout = createStaticLayout(dateLoc, datePaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            dateLayout.draw(canvas)
                            canvas.restore()
                            currentY += dateLayout.height + 3f

                            if (exp.description.isNotBlank()) {
                                val descLayout = createStaticLayout(exp.description, bodyPaint, contentWidth.toInt(), textAlignment)
                                canvas.save()
                                canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                                descLayout.draw(canvas)
                                canvas.restore()
                                currentY += descLayout.height + 8f
                            } else {
                                currentY += 5f
                            }
                        }
                    }
                }
                "education" -> {
                    if (resume.educations.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "التعليم والمؤهلات" else "Education", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        for (edu in resume.educations) {
                            val degInst = "${edu.degree}${if (edu.fieldOfStudy.isNotBlank()) " in ${edu.fieldOfStudy}" else ""} - ${edu.institution}"
                            val dateLoc = "${edu.startDate} – ${if (edu.currentlyStudying) (if (isRtl) "حتى الآن" else "Present") else edu.endDate} ${if (edu.location.isNotBlank()) " | ${edu.location}" else ""}"

                            val eduHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textPrimaryColor
                                textSize = 10.5f * resume.customization.fontSizeScale
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }
                            val eduDatePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textSecondaryColor
                                textSize = 8.5f * resume.customization.fontSizeScale
                                typeface = Typeface.DEFAULT
                            }

                            val hl = createStaticLayout(degInst, eduHeaderPaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            hl.draw(canvas)
                            canvas.restore()
                            currentY += hl.height + 2f

                            val dl = createStaticLayout(dateLoc, eduDatePaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            dl.draw(canvas)
                            canvas.restore()
                            currentY += dl.height + 3f

                            if (edu.description.isNotBlank()) {
                                val dsc = createStaticLayout(edu.description, bodyPaint, contentWidth.toInt(), textAlignment)
                                canvas.save()
                                canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                                dsc.draw(canvas)
                                canvas.restore()
                                currentY += dsc.height + 6f
                            } else {
                                currentY += 4f
                            }
                        }
                    }
                }
                "skills" -> {
                    if (resume.skills.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "المهارات" else "Skills", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        val skillsStr = resume.skills.joinToString(", ") { "${it.name}${if (it.level.isNotBlank()) " (${it.level})" else ""}" }
                        val skillsLayout = createStaticLayout(skillsStr, bodyPaint, contentWidth.toInt(), textAlignment)
                        canvas.save()
                        canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                        skillsLayout.draw(canvas)
                        canvas.restore()
                        currentY += skillsLayout.height + 10f
                    }
                }
                "projects" -> {
                    if (resume.projects.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "المشاريع" else "Projects", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        for (proj in resume.projects) {
                            val projTitle = "${proj.name}${if (proj.role.isNotBlank()) " (${proj.role})" else ""}${if (proj.technologies.isNotBlank()) " [${proj.technologies}]" else ""}"
                            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = textPrimaryColor
                                textSize = 10f * resume.customization.fontSizeScale
                                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            }
                            val tl = createStaticLayout(projTitle, titlePaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            tl.draw(canvas)
                            canvas.restore()
                            currentY += tl.height + 2f

                            if (proj.description.isNotBlank()) {
                                val dl = createStaticLayout(proj.description, bodyPaint, contentWidth.toInt(), textAlignment)
                                canvas.save()
                                canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                                dl.draw(canvas)
                                canvas.restore()
                                currentY += dl.height + 6f
                            }
                        }
                    }
                }
                "languages" -> {
                    if (resume.languages.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "اللغات" else "Languages", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        val langStr = resume.languages.joinToString("  •  ") { "${it.name} (${it.level})" }
                        val langLayout = createStaticLayout(langStr, bodyPaint, contentWidth.toInt(), textAlignment)
                        canvas.save()
                        canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                        langLayout.draw(canvas)
                        canvas.restore()
                        currentY += langLayout.height + 10f
                    }
                }
                "certificates" -> {
                    if (resume.certificates.isNotEmpty()) {
                        currentY = drawSectionHeader(canvas, if (isRtl) "الشهادات والدورات" else "Certifications", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
                        for (cert in resume.certificates) {
                            val certStr = "${cert.name} – ${cert.issuer}${if (cert.issueDate.isNotBlank()) " (${cert.issueDate})" else ""}"
                            val certLayout = createStaticLayout(certStr, bodyPaint, contentWidth.toInt(), textAlignment)
                            canvas.save()
                            canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
                            certLayout.draw(canvas)
                            canvas.restore()
                            currentY += certLayout.height + 4f
                        }
                        currentY += 6f
                    }
                }
            }
        }
    }

    private fun renderSidebarLayout(
        canvas: Canvas,
        resume: Resume,
        template: TemplateSpec,
        pageWidth: Int,
        pageHeight: Int,
        sidebarWidth: Float,
        sidebarOnStart: Boolean,
        marginTop: Float,
        marginBottom: Float,
        marginStart: Float,
        marginEnd: Float,
        primaryColor: Int,
        secondaryColor: Int,
        accentColor: Int,
        textPrimaryColor: Int,
        textSecondaryColor: Int
    ) {
        val sidebarX = if (sidebarOnStart) 18f else (pageWidth - sidebarWidth + 18f)
        val sidebarContentWidth = (sidebarWidth - 36f).toInt()
        val mainX = if (sidebarOnStart) (sidebarWidth + 24f) else 24f
        val mainContentWidth = (pageWidth - sidebarWidth - 48f).toInt()

        var sidebarY = marginTop + 16f
        var mainY = marginTop + 16f

        val isRtl = resume.isRtl
        val textAlignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL

        // Sidebar content: Photo, Contact Info, Skills, Languages, References
        // 1. Photo in Sidebar if enabled
        if (template.photo.enabled && resume.customization.showPhoto && !resume.personalInfo.profilePhotoUri.isNullOrBlank()) {
            drawProfilePhoto(canvas, resume.personalInfo.profilePhotoUri, sidebarX + (sidebarContentWidth - 60f) / 2, sidebarY, 60f, template.photo.shape)
            sidebarY += 70f
        }

        // Contact Section in Sidebar
        val sidebarHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = 11f * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val sidebarBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimaryColor
            textSize = 8.5f * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        sidebarY = drawSidebarSectionHeader(canvas, if (isRtl) "معلومات التواصل" else "Contact", sidebarX, sidebarY, sidebarContentWidth.toFloat(), primaryColor, isRtl)

        val contactItems = listOfNotNull(
            resume.personalInfo.email.takeIf { it.isNotBlank() }?.let { "✉ $it" },
            resume.personalInfo.phone.takeIf { it.isNotBlank() }?.let { "☎ $it" },
            resume.personalInfo.locationFormatted.takeIf { it.isNotBlank() }?.let { "📍 $it" },
            resume.personalInfo.linkedIn.takeIf { it.isNotBlank() }?.let { "🔗 $it" },
            resume.personalInfo.gitHub.takeIf { it.isNotBlank() }?.let { "💻 $it" },
            resume.personalInfo.portfolio.takeIf { it.isNotBlank() }?.let { "🌐 $it" }
        )
        for (item in contactItems) {
            val layout = createStaticLayout(item, sidebarBodyPaint, sidebarContentWidth, textAlignment)
            canvas.save()
            canvas.translate(sidebarX, sidebarY)
            layout.draw(canvas)
            canvas.restore()
            sidebarY += layout.height + 4f
        }
        sidebarY += 10f

        // Skills in Sidebar
        if (resume.skills.isNotEmpty()) {
            sidebarY = drawSidebarSectionHeader(canvas, if (isRtl) "المهارات" else "Skills", sidebarX, sidebarY, sidebarContentWidth.toFloat(), primaryColor, isRtl)
            for (skill in resume.skills) {
                val skillText = "• ${skill.name}${if (skill.level.isNotBlank()) " (${skill.level})" else ""}"
                val layout = createStaticLayout(skillText, sidebarBodyPaint, sidebarContentWidth, textAlignment)
                canvas.save()
                canvas.translate(sidebarX, sidebarY)
                layout.draw(canvas)
                canvas.restore()
                sidebarY += layout.height + 3f
            }
            sidebarY += 10f
        }

        // Languages in Sidebar
        if (resume.languages.isNotEmpty()) {
            sidebarY = drawSidebarSectionHeader(canvas, if (isRtl) "اللغات" else "Languages", sidebarX, sidebarY, sidebarContentWidth.toFloat(), primaryColor, isRtl)
            for (lang in resume.languages) {
                val langText = "• ${lang.name}: ${lang.level}"
                val layout = createStaticLayout(langText, sidebarBodyPaint, sidebarContentWidth, textAlignment)
                canvas.save()
                canvas.translate(sidebarX, sidebarY)
                layout.draw(canvas)
                canvas.restore()
                sidebarY += layout.height + 3f
            }
            sidebarY += 10f
        }

        // Main Column: Name, Title, Summary, Experience, Education, Projects, Certificates
        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = template.typography.nameSize * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = template.typography.titleSize * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimaryColor
            textSize = template.typography.bodySize * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        // Name
        val nameLayout = createStaticLayout(resume.personalInfo.fullName, namePaint, mainContentWidth, textAlignment)
        canvas.save()
        canvas.translate(mainX, mainY)
        nameLayout.draw(canvas)
        canvas.restore()
        mainY += nameLayout.height + 3f

        // Title
        if (resume.personalInfo.professionalTitle.isNotBlank()) {
            val titleLayout = createStaticLayout(resume.personalInfo.professionalTitle, titlePaint, mainContentWidth, textAlignment)
            canvas.save()
            canvas.translate(mainX, mainY)
            titleLayout.draw(canvas)
            canvas.restore()
            mainY += titleLayout.height + 10f
        }

        // Summary
        if (resume.summary.isNotBlank()) {
            mainY = drawSectionHeader(canvas, if (isRtl) "الملخص المهني" else "Professional Summary", mainY, mainX, 0f, mainContentWidth.toFloat(), primaryColor, template, isRtl)
            val summaryLayout = createStaticLayout(resume.summary, bodyPaint, mainContentWidth, textAlignment)
            canvas.save()
            canvas.translate(mainX, mainY)
            summaryLayout.draw(canvas)
            canvas.restore()
            mainY += summaryLayout.height + 10f
        }

        // Experience
        if (resume.experiences.isNotEmpty()) {
            mainY = drawSectionHeader(canvas, if (isRtl) "الخبرات المهنية" else "Experience", mainY, mainX, 0f, mainContentWidth.toFloat(), primaryColor, template, isRtl)
            for (exp in resume.experiences) {
                val roleCompany = "${exp.jobTitle} - ${exp.company}"
                val dateLoc = "${exp.startDate} – ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate} ${if (exp.location.isNotBlank()) " | ${exp.location}" else ""}"

                val rolePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textPrimaryColor
                    textSize = 10f * resume.customization.fontSizeScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textSecondaryColor
                    textSize = 8.5f * resume.customization.fontSizeScale
                    typeface = Typeface.DEFAULT
                }

                val rl = createStaticLayout(roleCompany, rolePaint, mainContentWidth, textAlignment)
                canvas.save()
                canvas.translate(mainX, mainY)
                rl.draw(canvas)
                canvas.restore()
                mainY += rl.height + 2f

                val dl = createStaticLayout(dateLoc, datePaint, mainContentWidth, textAlignment)
                canvas.save()
                canvas.translate(mainX, mainY)
                dl.draw(canvas)
                canvas.restore()
                mainY += dl.height + 3f

                if (exp.description.isNotBlank()) {
                    val dsc = createStaticLayout(exp.description, bodyPaint, mainContentWidth, textAlignment)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    dsc.draw(canvas)
                    canvas.restore()
                    mainY += dsc.height + 7f
                } else {
                    mainY += 4f
                }
            }
        }

        // Education
        if (resume.educations.isNotEmpty()) {
            mainY = drawSectionHeader(canvas, if (isRtl) "التعليم والمؤهلات" else "Education", mainY, mainX, 0f, mainContentWidth.toFloat(), primaryColor, template, isRtl)
            for (edu in resume.educations) {
                val degInst = "${edu.degree}${if (edu.fieldOfStudy.isNotBlank()) " in ${edu.fieldOfStudy}" else ""} - ${edu.institution}"
                val dateLoc = "${edu.startDate} – ${if (edu.currentlyStudying) (if (isRtl) "حتى الآن" else "Present") else edu.endDate}"

                val eduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textPrimaryColor
                    textSize = 10f * resume.customization.fontSizeScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val el = createStaticLayout(degInst, eduPaint, mainContentWidth, textAlignment)
                canvas.save()
                canvas.translate(mainX, mainY)
                el.draw(canvas)
                canvas.restore()
                mainY += el.height + 2f

                val dl = createStaticLayout(dateLoc, bodyPaint, mainContentWidth, textAlignment)
                canvas.save()
                canvas.translate(mainX, mainY)
                dl.draw(canvas)
                canvas.restore()
                mainY += dl.height + 6f
            }
        }

        // Projects
        if (resume.projects.isNotEmpty()) {
            mainY = drawSectionHeader(canvas, if (isRtl) "المشاريع" else "Projects", mainY, mainX, 0f, mainContentWidth.toFloat(), primaryColor, template, isRtl)
            for (proj in resume.projects) {
                val projTitle = "${proj.name}${if (proj.role.isNotBlank()) " (${proj.role})" else ""}"
                val pPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textPrimaryColor
                    textSize = 9.5f * resume.customization.fontSizeScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val pl = createStaticLayout(projTitle, pPaint, mainContentWidth, textAlignment)
                canvas.save()
                canvas.translate(mainX, mainY)
                pl.draw(canvas)
                canvas.restore()
                mainY += pl.height + 2f

                if (proj.description.isNotBlank()) {
                    val dl = createStaticLayout(proj.description, bodyPaint, mainContentWidth, textAlignment)
                    canvas.save()
                    canvas.translate(mainX, mainY)
                    dl.draw(canvas)
                    canvas.restore()
                    mainY += dl.height + 6f
                }
            }
        }
    }

    private fun renderHeaderSplitLayout(
        canvas: Canvas,
        resume: Resume,
        template: TemplateSpec,
        pageWidth: Int,
        pageHeight: Int,
        bannerHeight: Float,
        isRtl: Boolean,
        marginTop: Float,
        marginBottom: Float,
        marginStart: Float,
        marginEnd: Float,
        primaryColor: Int,
        secondaryColor: Int,
        accentColor: Int,
        textPrimaryColor: Int,
        textSecondaryColor: Int
    ) {
        // Draw Header on dark banner
        val textAlignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        val namePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 13f * resume.customization.fontSizeScale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bannerContactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB")
            textSize = 8.5f * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        var headerY = 24f
        val bannerContentWidth = (pageWidth - 56).toInt()

        val nameLayout = createStaticLayout(resume.personalInfo.fullName, namePaint, bannerContentWidth, textAlignment)
        canvas.save()
        canvas.translate(28f, headerY)
        nameLayout.draw(canvas)
        canvas.restore()
        headerY += nameLayout.height + 2f

        if (resume.personalInfo.professionalTitle.isNotBlank()) {
            val titleLayout = createStaticLayout(resume.personalInfo.professionalTitle, titlePaint, bannerContentWidth, textAlignment)
            canvas.save()
            canvas.translate(28f, headerY)
            titleLayout.draw(canvas)
            canvas.restore()
            headerY += titleLayout.height + 4f
        }

        val contacts = listOf(
            resume.personalInfo.email,
            resume.personalInfo.phone,
            resume.personalInfo.locationFormatted
        ).filter { it.isNotBlank() }.joinToString("  |  ")
        if (contacts.isNotBlank()) {
            val cl = createStaticLayout(contacts, bannerContactPaint, bannerContentWidth, textAlignment)
            canvas.save()
            canvas.translate(28f, headerY)
            cl.draw(canvas)
            canvas.restore()
        }

        // Rest of content below banner
        var currentY = bannerHeight + 16f
        val contentWidth = pageWidth - marginStart - marginEnd
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textPrimaryColor
            textSize = template.typography.bodySize * resume.customization.fontSizeScale
            typeface = Typeface.DEFAULT
        }

        // Summary
        if (resume.summary.isNotBlank()) {
            currentY = drawSectionHeader(canvas, if (isRtl) "الملخص المهني" else "Professional Summary", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
            val sl = createStaticLayout(resume.summary, bodyPaint, contentWidth.toInt(), textAlignment)
            canvas.save()
            canvas.translate(marginStart, currentY)
            sl.draw(canvas)
            canvas.restore()
            currentY += sl.height + 10f
        }

        // Experience
        if (resume.experiences.isNotEmpty()) {
            currentY = drawSectionHeader(canvas, if (isRtl) "الخبرات المهنية" else "Experience", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
            for (exp in resume.experiences) {
                val roleCompany = "${exp.jobTitle} - ${exp.company} (${exp.startDate} – ${if (exp.currentlyWorking) (if (isRtl) "الآن" else "Present") else exp.endDate})"
                val rp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textPrimaryColor
                    textSize = 10f * resume.customization.fontSizeScale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val rl = createStaticLayout(roleCompany, rp, contentWidth.toInt(), textAlignment)
                canvas.save()
                canvas.translate(marginStart, currentY)
                rl.draw(canvas)
                canvas.restore()
                currentY += rl.height + 2f

                if (exp.description.isNotBlank()) {
                    val dl = createStaticLayout(exp.description, bodyPaint, contentWidth.toInt(), textAlignment)
                    canvas.save()
                    canvas.translate(marginStart, currentY)
                    dl.draw(canvas)
                    canvas.restore()
                    currentY += dl.height + 8f
                }
            }
        }

        // Education
        if (resume.educations.isNotEmpty()) {
            currentY = drawSectionHeader(canvas, if (isRtl) "التعليم والمؤهلات" else "Education", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
            for (edu in resume.educations) {
                val degInst = "${edu.degree}${if (edu.fieldOfStudy.isNotBlank()) " in ${edu.fieldOfStudy}" else ""} - ${edu.institution}"
                val el = createStaticLayout(degInst, bodyPaint, contentWidth.toInt(), textAlignment)
                canvas.save()
                canvas.translate(marginStart, currentY)
                el.draw(canvas)
                canvas.restore()
                currentY += el.height + 5f
            }
        }

        // Skills
        if (resume.skills.isNotEmpty()) {
            currentY = drawSectionHeader(canvas, if (isRtl) "المهارات" else "Skills", currentY, marginStart, marginEnd, contentWidth, primaryColor, template, isRtl)
            val skillsStr = resume.skills.joinToString(", ") { "${it.name} (${it.level})" }
            val skl = createStaticLayout(skillsStr, bodyPaint, contentWidth.toInt(), textAlignment)
            canvas.save()
            canvas.translate(marginStart, currentY)
            skl.draw(canvas)
            canvas.restore()
            currentY += skl.height + 10f
        }
    }

    private fun drawSectionHeader(
        canvas: Canvas,
        title: String,
        currentY: Float,
        marginStart: Float,
        marginEnd: Float,
        contentWidth: Float,
        primaryColor: Int,
        template: TemplateSpec,
        isRtl: Boolean
    ): Float {
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = template.typography.sectionTitleSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textAlignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        val layout = createStaticLayout(title, titlePaint, contentWidth.toInt(), textAlignment)

        canvas.save()
        canvas.translate(if (isRtl) marginEnd else marginStart, currentY)
        layout.draw(canvas)
        canvas.restore()

        val headerHeight = layout.height + 3f

        // Underline or badge
        val underlinePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 1.2f
        }
        canvas.drawLine(marginStart, currentY + headerHeight, marginStart + contentWidth, currentY + headerHeight, underlinePaint)

        return currentY + headerHeight + 8f
    }

    private fun drawSidebarSectionHeader(
        canvas: Canvas,
        title: String,
        sidebarX: Float,
        currentY: Float,
        sidebarWidth: Float,
        primaryColor: Int,
        isRtl: Boolean
    ): Float {
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textAlignment = if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
        val layout = createStaticLayout(title, titlePaint, sidebarWidth.toInt(), textAlignment)

        canvas.save()
        canvas.translate(sidebarX, currentY)
        layout.draw(canvas)
        canvas.restore()

        val linePaint = Paint().apply {
            color = primaryColor
            strokeWidth = 1f
        }
        canvas.drawLine(sidebarX, currentY + layout.height + 2f, sidebarX + sidebarWidth, currentY + layout.height + 2f, linePaint)

        return currentY + layout.height + 6f
    }

    private fun drawProfilePhoto(
        canvas: Canvas,
        uriString: String,
        x: Float,
        y: Float,
        size: Float,
        shape: String
    ) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, size.toInt(), size.toInt(), true)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                if (shape == "circle") {
                    // Circle crop
                    val output = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
                    val c = Canvas(output)
                    val color = -0xbdbdbe
                    val rect = Rect(0, 0, size.toInt(), size.toInt())
                    val rectF = RectF(rect)
                    paint.isAntiAlias = true
                    c.drawARGB(0, 0, 0, 0)
                    paint.color = color
                    c.drawOval(rectF, paint)
                    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    c.drawBitmap(scaled, rect, rect, paint)
                    canvas.drawBitmap(output, x, y, null)
                } else {
                    canvas.drawBitmap(scaled, x, y, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createStaticLayout(
        text: CharSequence,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment
    ): StaticLayout {
        val safeWidth = max(width, 10)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, safeWidth)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, safeWidth, alignment, 1.15f, 0f, false)
        }
    }

    private fun parseColorSafely(hex: String?, fallback: Int): Int {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            fallback
        }
    }
}
