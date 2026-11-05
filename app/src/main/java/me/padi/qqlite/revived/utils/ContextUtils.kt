// ContextUtils.kt
package me.padi.qqlite.revived.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import java.util.Collections
import java.util.IdentityHashMap
import me.padi.qqlite.revived.ModuleMainKt


internal fun Context.findActivity(): Activity? {
    val visited = Collections.newSetFromMap(IdentityHashMap<Context, Boolean>())
    var current: Context? = this
    while (current != null && visited.add(current)) {
        if (current is Activity) return current
        current = (current as? ContextWrapper)?.baseContext
    }
    return null
}

internal fun Context.findActivityWindow(): Window? = findActivity()?.window

object ContextUtils {
    fun getMobileQQContext(): Context? {
        return try {
            val mobileQQClass = Class.forName("mqq.app.MobileQQ")
            val field = mobileQQClass.getDeclaredField("sMobileQQ")
            field.isAccessible = true
            field.get(null) as? Context
        } catch (e: Exception) {

            null
        }
    }


    fun getModuleContext(module: ModuleMainKt): Context {
        val hostContext = getMobileQQContext()
            ?: throw IllegalStateException("Cannot get MobileQQ context")
        return module.moduleContext(hostContext)
    }
}