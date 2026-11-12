package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class DocumentType {
    PDF,
    IMAGE,
    HTML
}

data class LibraryDocument(
    val id: String,
    val title: String,
    val subtitle: String,
    val fileType: DocumentType,
    val uriString: String? = null,
    val dateAdded: String,
    val pageCount: Int = 1,
    val sizeText: String = "1.2 MB",
    val textSnippet: String = "",
    val assetFilePath: String? = null
)

data class LibraryFolder(
    val id: String,
    val nameTamil: String,
    val nameEnglish: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val documents: MutableList<LibraryDocument> = mutableListOf()
)

object MyLibraryRepository {

    val folders: List<LibraryFolder> by lazy {
        listOf(
            // 1. தமிழ்
            LibraryFolder(
                id = "tamil",
                nameTamil = "தமிழ்",
                nameEnglish = "Tamil Literature & Grammar",
                description = "திருக்குறள், நன்னூல், செய்யுள், உரைநடை மற்றும் இலக்கியப் புத்தகங்கள்.",
                badge = "இலக்கியம்",
                icon = Icons.Default.MenuBook,
                primaryColor = Color(0xFF047857), // Emerald Green
                secondaryColor = Color(0xFF10B981),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "tam_mayangoli_nana",
                        title = "📖 தமிழ் மயங்கொலிகள் - ந, ன, ண கற்பித்தல் பாடத்திட்டம்",
                        subtitle = "நகர, னகர, ணகர உச்சரிப்பு, விதிகள், கதை & வினாடி வினா",
                        fileType = DocumentType.HTML,
                        dateAdded = "12 Nov 2026",
                        pageCount = 6,
                        sizeText = "255 KB",
                        textSnippet = "ந (தந்நகரம்), ன (றன்னகரம்), ண (டண்ணகரம்) பிறக்கும் விதம், எழுத்து நண்பர்கள் கதை மற்றும் பொருள் வேறுபாடு பயிற்சி.",
                        assetFilePath = "file:///android_asset/tamil_mayangoligal_na_na_na.html"
                    ),
                    LibraryDocument(
                        id = "tam_mayangoli",
                        title = "📖 தமிழ் மயங்கொலிகள் கற்பித்தல் புத்தகம்",
                        subtitle = "ல, ள, ழ | ர, ற | ந, ண, ன வேறுபாடுகள் & விதிகள்",
                        fileType = DocumentType.HTML,
                        dateAdded = "12 Nov 2026",
                        pageCount = 8,
                        sizeText = "39 KB",
                        textSnippet = "ல, ள, ழ, ர, ற, ந, ண, ன மயங்கொலி வேறுபாடுகள், உச்சரிப்பு முறைகள், இலக்கண விதிகள் மற்றும் நாக்கு விளையாட்டுப் பாடத்திட்டம்.",
                        assetFilePath = "file:///android_asset/tamil_mayangoligal_full_book.html"
                    ),
                    LibraryDocument(
                        id = "tam_1",
                        title = "திருக்குறள் எளிய உரை",
                        subtitle = "அறத்துப்பால் மற்றும் பொருட்பால் விளக்கக் குறிப்பேடு",
                        fileType = DocumentType.PDF,
                        dateAdded = "26 Oct 2026",
                        pageCount = 48,
                        sizeText = "2.4 MB",
                        textSnippet = "அகர முதல எழுத்தெல்லாம் ஆதி பகவன் முதற்றே உலகு."
                    ),
                    LibraryDocument(
                        id = "tam_2",
                        title = "தமிழ் இலக்கண வாய்பாடுகள்",
                        subtitle = "எழுத்து, சொல், பொருள், யாப்பு, அணி அடிப்படைகள்",
                        fileType = DocumentType.PDF,
                        dateAdded = "24 Oct 2026",
                        pageCount = 24,
                        sizeText = "1.8 MB",
                        textSnippet = "தமிழ் எழுத்துகள் முதல் மற்றும் சார்பெழுத்துகள் என இருவகைப்படும்."
                    )
                )
            ),

            // 2. English (Includes Spoken English)
            LibraryFolder(
                id = "english",
                nameTamil = "English",
                nameEnglish = "English Grammar & Spoken",
                description = "Prose, Poetry, Supplementary, Grammar Rules & Spoken English 100 Sentences.",
                badge = "Grammar & Spoken",
                icon = Icons.Default.AutoStories,
                primaryColor = Color(0xFF1D4ED8), // Royal Blue
                secondaryColor = Color(0xFF3B82F6),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "eng_1",
                        title = "English Grammar Handbook",
                        subtitle = "Parts of speech, active & passive voice, direct-indirect speech",
                        fileType = DocumentType.PDF,
                        dateAdded = "26 Oct 2026",
                        pageCount = 36,
                        sizeText = "3.1 MB",
                        textSnippet = "A noun is a word that names a person, place, thing, or idea."
                    ),
                    LibraryDocument(
                        id = "spk_1",
                        title = "Spoken English Daily 100 Sentences",
                        subtitle = "தினசரி பயன்படுத்தும் 100 வாக்கியங்களின் முழுமையான அச்சுப் பிரதி",
                        fileType = DocumentType.PDF,
                        dateAdded = "26 Oct 2026",
                        pageCount = 20,
                        sizeText = "1.4 MB",
                        textSnippet = "Daily English sentences with Tamil meanings and audio tips."
                    )
                )
            ),

            // 3. Maths (Includes Mental Maths)
            LibraryFolder(
                id = "maths",
                nameTamil = "Maths",
                nameEnglish = "Mathematics & Mental Tricks",
                description = "வாய்பாடுகள், இயற்கணிதம், மனக்கணக்கு 500 குறுக்குவழிகள் மற்றும் வினாத்தாள்கள்.",
                badge = "சூத்திரங்கள் & மனக்கணக்கு",
                icon = Icons.Default.Calculate,
                primaryColor = Color(0xFFEA580C), // Warm Orange
                secondaryColor = Color(0xFFF97316),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "mat_1",
                        title = "கணித சூத்திரங்களின் அட்டவணை",
                        subtitle = "அனைத்து முக்கிய சூத்திரங்கள் & குறுக்குவழிகள்",
                        fileType = DocumentType.PDF,
                        dateAdded = "25 Oct 2026",
                        pageCount = 18,
                        sizeText = "1.5 MB",
                        textSnippet = "(a + b)² = a² + 2ab + b² | பரப்பளவு = நீளம் × அகலம்"
                    ),
                    LibraryDocument(
                        id = "mm_1",
                        title = "மனக்கணக்கு 1 விநாடி பெருக்கல் குறுக்குவழிகள்",
                        subtitle = "வேத கணித எளிய வழிமுறைகள் மற்றும் பயிற்சிகள்",
                        fileType = DocumentType.PDF,
                        dateAdded = "24 Oct 2026",
                        pageCount = 15,
                        sizeText = "1.1 MB",
                        textSnippet = "எண்களை மனதிலேயே கூட்டிப் பெருக்கும் எளிய சூத்திரங்கள்."
                    )
                )
            ),

            // 4. Science
            LibraryFolder(
                id = "science",
                nameTamil = "Science",
                nameEnglish = "Physics, Chemistry & Biology",
                description = "இயற்பியல் சோதனைகள், வேதியியல் வினைகள், உயிரியல் வரைபடங்கள்.",
                badge = "அறிவியல் குறிப்புகள்",
                icon = Icons.Default.Science,
                primaryColor = Color(0xFF0F766E), // Teal
                secondaryColor = Color(0xFF14B8A6),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "sci_1",
                        title = "அறிவியல் வரைபடங்கள் தொகுப்பு",
                        subtitle = "தாவர செல், மனித இதயம், நீர் சுழற்சி படங்கள்",
                        fileType = DocumentType.IMAGE,
                        dateAdded = "22 Oct 2026",
                        pageCount = 12,
                        sizeText = "4.2 MB",
                        textSnippet = "மனித இதயம் நான்கு அறைகளைக் கொண்டது."
                    )
                )
            ),

            // 5. Social
            LibraryFolder(
                id = "social",
                nameTamil = "Social",
                nameEnglish = "History, Geography & Civics",
                description = "வரலாற்று வரைபடங்கள், புவியியல் நில வரைபடங்கள், குடிமையியல் சட்டங்கள்.",
                badge = "வரைபடங்கள் & வரலாறு",
                icon = Icons.Default.Public,
                primaryColor = Color(0xFFB91C1C), // Deep Crimson
                secondaryColor = Color(0xFFEF4444),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "soc_1",
                        title = "இந்திய வரலாற்று காலக்கோடு",
                        subtitle = "பண்டைய இந்தியா, சோழர் காலம் முதல் விடுதலைப் போராட்டம் வரை",
                        fileType = DocumentType.PDF,
                        dateAdded = "21 Oct 2026",
                        pageCount = 16,
                        sizeText = "2.0 MB",
                        textSnippet = "மௌரியப் பேரரசு, சோழர்களின் கப்பற்படை மற்றும் சுதந்திரப் போராட்டம்."
                    )
                )
            ),

            // 6. Others
            LibraryFolder(
                id = "others",
                nameTamil = "Others",
                nameEnglish = "General Knowledge & Handouts",
                description = "பொது அறிவு, கைவினைப் படங்கள், தேர்வு அட்டவணைகள் மற்றும் பிற ஆவணங்கள்.",
                badge = "பொதுக் குறிப்புகள்",
                icon = Icons.Default.FolderSpecial,
                primaryColor = Color(0xFF334155), // Slate
                secondaryColor = Color(0xFF64748B),
                documents = mutableListOf(
                    LibraryDocument(
                        id = "oth_1",
                        title = "பள்ளி தேர்வு கால அட்டவணை 2026",
                        subtitle = "காலாண்டு, அரையாண்டு தேர்வு தேதிகள் மற்றும் பாடத்திட்டம்",
                        fileType = DocumentType.IMAGE,
                        dateAdded = "20 Oct 2026",
                        pageCount = 1,
                        sizeText = "850 KB",
                        textSnippet = "தேர்வு தொடங்கும் நேரம் மற்றும் விடைத்தாள் விநியோக வழிமுறைகள்."
                    )
                )
            )
        )
    }

    fun addDocumentToFolder(folderId: String, doc: LibraryDocument) {
        val folder = folders.find { it.id == folderId }
        folder?.documents?.add(0, doc)
    }

    fun removeDocument(folderId: String, docId: String) {
        val folder = folders.find { it.id == folderId }
        folder?.documents?.removeAll { it.id == docId }
    }
}
