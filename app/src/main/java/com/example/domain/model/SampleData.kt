package com.example.domain.model

import java.util.UUID

object SampleData {

    fun createEnglishSampleResume(templateId: String = "modern_green"): Resume {
        return Resume(
            id = UUID.randomUUID().toString(),
            title = "Software Engineer Resume",
            templateId = templateId,
            language = "en",
            createdAt = System.currentTimeMillis() - 86400000 * 3,
            updatedAt = System.currentTimeMillis() - 3600000 * 2,
            personalInfo = PersonalInformation(
                firstName = "Saher",
                middleName = "",
                lastName = "Qaid",
                professionalTitle = "Senior Software Engineer & Architect",
                email = "saher.qaid@example.com",
                phone = "+1 (555) 382-9012",
                city = "San Francisco",
                country = "United States",
                address = "500 Howard Street, Suite 400",
                website = "https://saherqaid.dev",
                linkedIn = "linkedin.com/in/saherqaid",
                gitHub = "github.com/saherqaid",
                portfolio = "saherqaid.dev/portfolio"
            ),
            summary = "Senior Software Engineer with 7+ years of experience architecting high-scale distributed systems, modern mobile applications, and resilient cloud backends. Proven track record leading cross-functional teams to deliver zero-downtime microservices and high-performance Android solutions.",
            experiences = listOf(
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "Apex Cloud Technologies",
                    jobTitle = "Lead Mobile & Backend Architect",
                    location = "San Francisco, CA",
                    startDate = "2022-03",
                    endDate = "",
                    currentlyWorking = true,
                    description = "• Architected offline-first Android applications with Kotlin Jetpack Compose and Room serving 500k+ active users.\n• Designed scalable RESTful microservices with 99.99% uptime and reduced latency by 35%.\n• Mentored 8 junior and mid-level software engineers on architectural best practices and CI/CD automation.",
                    sortOrder = 0
                ),
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "Nexis Global Solutions",
                    jobTitle = "Senior Full-Stack Developer",
                    location = "Austin, TX",
                    startDate = "2019-06",
                    endDate = "2022-02",
                    currentlyWorking = false,
                    description = "• Spearheaded core payment processing integrations handling over $20M in monthly volume securely.\n• Modernized legacy monolithic codebase into modular domain-driven architectures.\n• Implemented automated unit and UI testing pipelines increasing test coverage to 92%.",
                    sortOrder = 1
                )
            ),
            educations = listOf(
                Education(
                    id = UUID.randomUUID().toString(),
                    institution = "University of California, Berkeley",
                    degree = "Bachelor of Science",
                    fieldOfStudy = "Computer Science",
                    location = "Berkeley, CA",
                    startDate = "2015-09",
                    endDate = "2019-05",
                    currentlyStudying = false,
                    description = "Graduated with Honors. Dean's Honor List for 6 consecutive semesters. Focus on Distributed Systems & Algorithms.",
                    sortOrder = 0
                )
            ),
            skills = listOf(
                Skill(id = UUID.randomUUID().toString(), name = "Kotlin & Jetpack Compose", level = "Expert", sortOrder = 0),
                Skill(id = UUID.randomUUID().toString(), name = "Android SDK & Architecture", level = "Expert", sortOrder = 1),
                Skill(id = UUID.randomUUID().toString(), name = "System Architecture", level = "Expert", sortOrder = 2),
                Skill(id = UUID.randomUUID().toString(), name = "REST & GraphQL APIs", level = "Advanced", sortOrder = 3),
                Skill(id = UUID.randomUUID().toString(), name = "PostgreSQL & SQLite Room", level = "Advanced", sortOrder = 4),
                Skill(id = UUID.randomUUID().toString(), name = "CI/CD & Cloud DevOps", level = "Intermediate", sortOrder = 5)
            ),
            languages = listOf(
                LanguageSkill(id = UUID.randomUUID().toString(), name = "English", level = "Fluent", sortOrder = 0),
                LanguageSkill(id = UUID.randomUUID().toString(), name = "Arabic", level = "Native", sortOrder = 1)
            ),
            projects = listOf(
                Project(
                    id = UUID.randomUUID().toString(),
                    name = "ResumeCraft Mobile Engine",
                    role = "Lead Developer",
                    technologies = "Kotlin, Jetpack Compose, Room, PDF Engine",
                    url = "https://github.com/saherqaid/resumecraft",
                    startDate = "2024-01",
                    endDate = "2024-06",
                    description = "Offline-first dynamic template rendering engine capable of converting declarative JSON specifications into vector PDF documents.",
                    sortOrder = 0
                )
            ),
            certificates = listOf(
                Certificate(
                    id = UUID.randomUUID().toString(),
                    name = "Google Certified Associate Android Developer",
                    issuer = "Google Developers",
                    issueDate = "2023-08",
                    credentialId = "AAD-8492049",
                    credentialUrl = "https://google.com/verify/aad-8492049",
                    sortOrder = 0
                )
            ),
            references = listOf(
                Reference(
                    id = UUID.randomUUID().toString(),
                    name = "Dr. Marcus Vance",
                    jobTitle = "VP of Engineering",
                    company = "Apex Cloud Technologies",
                    email = "m.vance@apexcloud.io",
                    phone = "+1 (555) 749-2041",
                    sortOrder = 0
                )
            )
        )
    }

    fun createArabicSampleResume(templateId: String = "arabic_professional"): Resume {
        return Resume(
            id = UUID.randomUUID().toString(),
            title = "سيرة ذاتية - مهندس برمجيات",
            templateId = templateId,
            language = "ar",
            createdAt = System.currentTimeMillis() - 86400000 * 2,
            updatedAt = System.currentTimeMillis() - 3600000,
            personalInfo = PersonalInformation(
                firstName = "ساهر",
                middleName = "",
                lastName = "قائد",
                professionalTitle = "مهندس برمجيات ومطور أنظمة خبيرة",
                email = "saher.qaid@example.com",
                phone = "+966 50 123 4567",
                city = "الرياض",
                country = "المملكة العربية السعودية",
                address = "طريق الملك فهد، حي العليا",
                website = "https://saherqaid.dev",
                linkedIn = "linkedin.com/in/saherqaid",
                gitHub = "github.com/saherqaid",
                portfolio = "saherqaid.dev/ar"
            ),
            summary = "مهندس برمجيات أول بخبرة تزيد عن 7 سنوات في هندسة النظم السحابية الموزعة، وبناء تطبيقات الهواتف الذكية عالية الأداء والأنظمة الآمنة. أمتلك سجلاً حافلاً في قيادة الفرق التقنية وإطلاق منصات رقمية تخدم مئات الآلاف من المستخدمين بكفاءة واعتمادية تامة.",
            experiences = listOf(
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "شركة آفاق السحابية للتقنية",
                    jobTitle = "معماري برمجيات أول",
                    location = "الرياض، السعودية",
                    startDate = "2022-01",
                    endDate = "",
                    currentlyWorking = true,
                    description = "• قيادة وتطوير تطبيقات أندرويد متقدمة باستخدام Kotlin وJetpack Compose تخدم أكثر من 500 ألف مستخدم.\n• تصميم بنية تحتية سحابية مصغرة (Microservices) بتقنيات حديثة رفعت سرعة الاستجابة بنسبة 40%.\n• الإشراف على فريق هندسي وتطبيق أفضل ممارسات اختبار البرمجيات والجودة المستمرة CI/CD.",
                    sortOrder = 0
                ),
                Experience(
                    id = UUID.randomUUID().toString(),
                    company = "حلول التحول الرقمي المتقدمة",
                    jobTitle = "مطور تطبيقات وأنظمة متكاملة",
                    location = "دبي، الإمارات",
                    startDate = "2019-07",
                    endDate = "2021-12",
                    currentlyWorking = false,
                    description = "• تطوير بوابات دفع إلكتروني وأنظمة تجارة رقمية مع معالجة آمنة لملايين المعاملات شهرياً.\n• إعادة هيكلة الأنظمة القديمة إلى بنية برمجية معيارية قابلة للتوسع والتطوير السريع.\n• بناء واجهات برمجية RESTful وGraphQL فائقة السرعة.",
                    sortOrder = 1
                )
            ),
            educations = listOf(
                Education(
                    id = UUID.randomUUID().toString(),
                    institution = "جامعة الملك فهد للبترول والمعادن",
                    degree = "بكالوريوس علوم",
                    fieldOfStudy = "هندسة البرمجيات وعلوم الحاسب",
                    location = "الظهران، السعودية",
                    startDate = "2015-09",
                    endDate = "2019-06",
                    currentlyStudying = false,
                    description = "مرتبة الشرف الأولى. التركيز على الخوارزميات المتقدمة وهندسة النظم الموزعة وأمن المعلومات.",
                    sortOrder = 0
                )
            ),
            skills = listOf(
                Skill(id = UUID.randomUUID().toString(), name = "لغة Kotlin وJetpack Compose", level = "خبير", sortOrder = 0),
                Skill(id = UUID.randomUUID().toString(), name = "هندسة النظم والتطبيقات", level = "خبير", sortOrder = 1),
                Skill(id = UUID.randomUUID().toString(), name = "قواعد البيانات SQLite وPostgreSQL", level = "متقدم", sortOrder = 2),
                Skill(id = UUID.randomUUID().toString(), name = "تطوير واجهات REST وGraphQL", level = "متقدم", sortOrder = 3),
                Skill(id = UUID.randomUUID().toString(), name = "أتمتة العمليات DevOps وCI/CD", level = "متقدم", sortOrder = 4),
                Skill(id = UUID.randomUUID().toString(), name = "أنظمة التخزين المحلي والأمان", level = "متقدم", sortOrder = 5)
            ),
            languages = listOf(
                LanguageSkill(id = UUID.randomUUID().toString(), name = "العربية", level = "اللغة الأم", sortOrder = 0),
                LanguageSkill(id = UUID.randomUUID().toString(), name = "الإنجليزية", level = "طليق (Fluent)", sortOrder = 1)
            ),
            projects = listOf(
                Project(
                    id = UUID.randomUUID().toString(),
                    name = "نظام توليد السير الذاتية ResumeCraft",
                    role = "المطور الرئيسي والمعماري التقني",
                    technologies = "Kotlin, Jetpack Compose, Room, PDF Engine",
                    url = "https://github.com/saherqaid/resumecraft",
                    startDate = "2024-01",
                    endDate = "2024-06",
                    description = "محرك متطور لقراءة قوالب JSON التفاعلية وتوليد مستندات PDF متوافقة مع أنظمة الفرز الآلي ومعايير التصميم الحديثة.",
                    sortOrder = 0
                )
            ),
            certificates = listOf(
                Certificate(
                    id = UUID.randomUUID().toString(),
                    name = "مطور أندرويد معتمد من Google",
                    issuer = "Google Developers",
                    issueDate = "2023-08",
                    credentialId = "AAD-8492049",
                    credentialUrl = "https://google.com/verify/aad-8492049",
                    sortOrder = 0
                )
            ),
            references = listOf(
                Reference(
                    id = UUID.randomUUID().toString(),
                    name = "م. فيصل الشمري",
                    jobTitle = "نائب الرئيس لقطاع الهندسة والتقنية",
                    company = "شركة آفاق السحابية",
                    email = "f.shammari@afaqcloud.sa",
                    phone = "+966 55 987 6543",
                    sortOrder = 0
                )
            )
        )
    }
}
