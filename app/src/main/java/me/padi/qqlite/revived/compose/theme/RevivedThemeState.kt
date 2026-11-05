package me.padi.qqlite.revived.compose.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class RevivedUiMode { Miuix, Material }
enum class RevivedThemeMode { System, Light, Dark }
enum class RevivedThemePreset { Blue, Green, Purple }
enum class RevivedEffectLevel { Off, Low, Medium, High }

@Immutable
data class RevivedThemePreference(
    val uiMode: RevivedUiMode = RevivedUiMode.Miuix,
    val mode: RevivedThemeMode = RevivedThemeMode.System,
    val preset: RevivedThemePreset = RevivedThemePreset.Blue,
    val enableLiquidGlass: Boolean = true,
    val glassLevel: RevivedEffectLevel = RevivedEffectLevel.Low,
    val frostLevel: RevivedEffectLevel = RevivedEffectLevel.Medium,
) {
    val hasGlassEffects: Boolean
        get() = enableLiquidGlass &&
            (glassLevel != RevivedEffectLevel.Off || frostLevel != RevivedEffectLevel.Off)
}

/** 进程内可观察状态；使用当前应用的私有存储，不跨 QQ / 模块或进程同步。 */
object RevivedThemeState {
    var preference by mutableStateOf(RevivedThemePreference())
        private set

    private var preferences: SharedPreferences? = null

    @Synchronized
    internal fun initialize(context: Context) {
        if (preferences != null) return
        val storage = (context.applicationContext ?: context).getSharedPreferences(
            "revived_theme", Context.MODE_PRIVATE
        )
        val values = storage.all
        preference = RevivedThemePreference(
            uiMode = enumValue(values["ui_mode"], RevivedUiMode.Miuix),
            mode = enumValue(values["theme_mode"], RevivedThemeMode.System),
            preset = enumValue(values["theme_preset"], RevivedThemePreset.Blue),
            enableLiquidGlass = values["liquid_glass"] as? Boolean ?: true,
            glassLevel = enumValue(values["glass_level"], RevivedEffectLevel.Low),
            frostLevel = enumValue(values["frost_level"], RevivedEffectLevel.Medium),
        )
        preferences = storage
    }

    fun updateUiMode(context: Context, value: RevivedUiMode) =
        update(context) { it.copy(uiMode = value) }

    fun updateMode(context: Context, value: RevivedThemeMode) =
        update(context) { it.copy(mode = value) }

    fun updatePreset(context: Context, value: RevivedThemePreset) =
        update(context) { it.copy(preset = value) }

    fun updateLiquidGlassEnabled(context: Context, value: Boolean) =
        update(context) { it.copy(enableLiquidGlass = value) }

    fun updateGlassLevel(context: Context, value: RevivedEffectLevel) =
        update(context) { it.copy(glassLevel = value) }

    fun updateFrostLevel(context: Context, value: RevivedEffectLevel) =
        update(context) { it.copy(frostLevel = value) }

    @Synchronized
    private fun update(
        context: Context,
        transform: (RevivedThemePreference) -> RevivedThemePreference,
    ) {
        initialize(context)
        val next = transform(preference)
        preference = next
        checkNotNull(preferences).edit()
            .putString("ui_mode", next.uiMode.name)
            .putString("theme_mode", next.mode.name)
            .putString("theme_preset", next.preset.name)
            .putBoolean("liquid_glass", next.enableLiquidGlass)
            .putString("glass_level", next.glassLevel.name)
            .putString("frost_level", next.frostLevel.name)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumValue(value: Any?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback
}
