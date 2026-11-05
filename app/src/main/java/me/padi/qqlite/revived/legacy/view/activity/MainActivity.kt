package me.padi.qqlite.revived.legacy.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedService.OnScopeEventListener
import me.padi.qqlite.revived.App
import me.padi.qqlite.revived.compose.screens.settings.ModuleSettingsScreen
import me.padi.qqlite.revived.compose.theme.RevivedTheme
import me.padi.qqlite.revived.compose.theme.RevivedThemeState
import me.padi.qqlite.revived.di.RevivedKoin
import me.padi.qqlite.revived.shared.viewmodel.module.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity(), App.ServiceStateListener {
    private var service: XposedService? = null
    private val viewModel: MainViewModel by viewModel()

    private val scopeCallback = object : OnScopeEventListener {
        override fun onScopeRequestApproved(approved: List<String>) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, "onScopeRequestApproved: $approved", Toast.LENGTH_SHORT
                ).show()
                viewModel.updateScope(service?.scope)
            }
        }

        override fun onScopeRequestFailed(message: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, "onScopeRequestFailed: $message", Toast.LENGTH_SHORT
                ).show()
                viewModel.updateScope(service?.scope)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        RevivedKoin.ensureStarted(this)
        super.onCreate(savedInstanceState)
        setContent {
            RevivedTheme {
                val uiState by viewModel.uiState.collectAsState()
                ModuleSettingsScreen(
                    uiState = uiState,
                    themePreference = RevivedThemeState.preference,
                    onRequestScopeClick = ::requestScope,
                    onReloadClick = ::hotReloadTargets,
                    onUiModeChange = { uiMode ->
                        RevivedThemeState.updateUiMode(this@MainActivity, uiMode)
                    },
                    onThemeModeChange = { mode ->
                        RevivedThemeState.updateMode(this@MainActivity, mode)
                    },
                    onThemePresetChange = { preset ->
                        RevivedThemeState.updatePreset(this@MainActivity, preset)
                    },
                    onLiquidGlassChange = { enabled ->
                        RevivedThemeState.updateLiquidGlassEnabled(this@MainActivity, enabled)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        App.addServiceStateListener(this, true)
    }

    override fun onStop() {
        App.removeServiceStateListener(this)
        super.onStop()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        this.service = service
        runOnUiThread {
            viewModel.updateService(service)
        }
    }

    private fun hotReloadTargets() {
        Toast.makeText(this, "为避免界面与 Hook 失效，请强行停止 QQ 后重新打开", Toast.LENGTH_LONG).show()
    }

    private fun requestScope() {
        service?.requestScope(listOf(TARGET_PACKAGE), scopeCallback)
    }

    private companion object {
        const val TARGET_PACKAGE = "com.tencent.qqlite"
    }
}
