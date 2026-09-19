package ir.courseplanner.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppColorTheme(
    val id: String,
    val titleFa: String,
    val primaryColor: Color,
    val primaryContainerColor: Color,
    val secondaryColor: Color,
    val darkPrimaryColor: Color,
    val darkPrimaryContainerColor: Color
) {
    INDIGO(
        id = "indigo",
        titleFa = "نیلی دانشگاهی",
        primaryColor = Color(0xFF2563EB),
        primaryContainerColor = Color(0xFFEFF6FF),
        secondaryColor = Color(0xFF0D9488),
        darkPrimaryColor = Color(0xFF60A5FA),
        darkPrimaryContainerColor = Color(0xFF1E3A8A)
    ),
    EMERALD(
        id = "emerald",
        titleFa = "زمردی جنگلی",
        primaryColor = Color(0xFF059669),
        primaryContainerColor = Color(0xFFECFDF5),
        secondaryColor = Color(0xFF0284C7),
        darkPrimaryColor = Color(0xFF34D399),
        darkPrimaryContainerColor = Color(0xFF064E3B)
    ),
    VIOLET(
        id = "violet",
        titleFa = "ارغوانی رویال",
        primaryColor = Color(0xFF7C3AED),
        primaryContainerColor = Color(0xFFF5F3FF),
        secondaryColor = Color(0xFFDB2777),
        darkPrimaryColor = Color(0xFFA78BFA),
        darkPrimaryContainerColor = Color(0xFF4C1D95)
    ),
    AMBER(
        id = "amber",
        titleFa = "غروب کهربایی",
        primaryColor = Color(0xFFD97706),
        primaryContainerColor = Color(0xFFFFFBEB),
        secondaryColor = Color(0xFFEA580C),
        darkPrimaryColor = Color(0xFFFBBF24),
        darkPrimaryContainerColor = Color(0xFF78350F)
    ),
    OCEAN(
        id = "ocean",
        titleFa = "اقیانوسی فیروزه‌ای",
        primaryColor = Color(0xFF0891B2),
        primaryContainerColor = Color(0xFFECFEFF),
        secondaryColor = Color(0xFF2563EB),
        darkPrimaryColor = Color(0xFF22D3EE),
        darkPrimaryContainerColor = Color(0xFF164E63)
    ),
    ROSE(
        id = "rose",
        titleFa = "یاقوت سرخ",
        primaryColor = Color(0xFFE11D48),
        primaryContainerColor = Color(0xFFFFF1F2),
        secondaryColor = Color(0xFF9333EA),
        darkPrimaryColor = Color(0xFFFB7185),
        darkPrimaryContainerColor = Color(0xFF881337)
    ),
    SLATE(
        id = "slate",
        titleFa = "نوک‌مدادی مینیمال",
        primaryColor = Color(0xFF475569),
        primaryContainerColor = Color(0xFFF1F5F9),
        secondaryColor = Color(0xFF0F766E),
        darkPrimaryColor = Color(0xFF94A3B8),
        darkPrimaryContainerColor = Color(0xFF1E293B)
    );

    companion object {
        fun fromId(id: String?): AppColorTheme {
            return values().find { it.id == id } ?: INDIGO
        }
    }
}

enum class ThemeMode(val id: String, val titleFa: String) {
    SYSTEM("system", "پیروی از سیستم"),
    LIGHT("light", "همیشه روشن"),
    DARK("dark", "همیشه تاریک");

    companion object {
        fun fromId(id: String?): ThemeMode {
            return values().find { it.id == id } ?: SYSTEM
        }
    }
}

enum class TimetableDensity(val id: String, val titleFa: String, val slotHeightDp: Int) {
    COMPACT("compact", "فشرده", 46),
    STANDARD("standard", "استاندارد", 58),
    SPACIOUS("spacious", "گسترده", 70);

    companion object {
        fun fromId(id: String?): TimetableDensity {
            return values().find { it.id == id } ?: STANDARD
        }
    }
}

data class UserPreferences(
    val theme: AppColorTheme = AppColorTheme.INDIGO,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val studentName: String = "",
    val major: String = "",
    val semesterName: String = "نیم‌سال اول ۱۴۰۳-۱۴۰۴",
    val creditTarget: Int = 20,
    val showThursday: Boolean = true,
    val timetableDensity: TimetableDensity = TimetableDensity.STANDARD
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("planner_user_prefs", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val themeId = prefs.getString(KEY_THEME, AppColorTheme.INDIGO.id)
        val themeModeId = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.id)
        val studentName = prefs.getString(KEY_STUDENT_NAME, "") ?: ""
        val major = prefs.getString(KEY_MAJOR, "") ?: ""
        val semesterName = prefs.getString(KEY_SEMESTER_NAME, "نیم‌سال اول ۱۴۰۳-۱۴۰۴") ?: "نیم‌سال اول ۱۴۰۳-۱۴۰۴"
        val creditTarget = prefs.getInt(KEY_CREDIT_TARGET, 20)
        val showThursday = prefs.getBoolean(KEY_SHOW_THURSDAY, true)
        val densityId = prefs.getString(KEY_DENSITY, TimetableDensity.STANDARD.id)

        return UserPreferences(
            theme = AppColorTheme.fromId(themeId),
            themeMode = ThemeMode.fromId(themeModeId),
            studentName = studentName,
            major = major,
            semesterName = semesterName,
            creditTarget = creditTarget,
            showThursday = showThursday,
            timetableDensity = TimetableDensity.fromId(densityId)
        )
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit().putString(KEY_THEME, theme.id).apply()
        _preferences.value = _preferences.value.copy(theme = theme)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.id).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setStudentProfile(name: String, major: String, semester: String) {
        prefs.edit()
            .putString(KEY_STUDENT_NAME, name.trim())
            .putString(KEY_MAJOR, major.trim())
            .putString(KEY_SEMESTER_NAME, semester.trim().ifBlank { "نیم‌سال اول ۱۴۰۳-۱۴۰۴" })
            .apply()
        _preferences.value = _preferences.value.copy(
            studentName = name.trim(),
            major = major.trim(),
            semesterName = semester.trim().ifBlank { "نیم‌سال اول ۱۴۰۳-۱۴۰۴" }
        )
    }

    fun setCreditTarget(target: Int) {
        val validTarget = target.coerceIn(10, 30)
        prefs.edit().putInt(KEY_CREDIT_TARGET, validTarget).apply()
        _preferences.value = _preferences.value.copy(creditTarget = validTarget)
    }

    fun setShowThursday(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_THURSDAY, show).apply()
        _preferences.value = _preferences.value.copy(showThursday = show)
    }

    fun setTimetableDensity(density: TimetableDensity) {
        prefs.edit().putString(KEY_DENSITY, density.id).apply()
        _preferences.value = _preferences.value.copy(timetableDensity = density)
    }

    fun isReleaseCleanDone(): Boolean = prefs.getBoolean(KEY_RELEASE_CLEAN, false)

    fun markReleaseCleanDone() {
        prefs.edit().putBoolean(KEY_RELEASE_CLEAN, true).apply()
    }

    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_STUDENT_NAME = "student_name"
        private const val KEY_MAJOR = "student_major"
        private const val KEY_SEMESTER_NAME = "semester_name"
        private const val KEY_CREDIT_TARGET = "credit_target"
        private const val KEY_SHOW_THURSDAY = "show_thursday"
        private const val KEY_DENSITY = "timetable_density"
        private const val KEY_RELEASE_CLEAN = "release_clean_courses_v1"
    }
}
