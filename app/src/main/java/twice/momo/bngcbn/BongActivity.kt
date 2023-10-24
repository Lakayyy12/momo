package twice.momo.bngcbn

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.webkit.WebSettings
import twice.momo.bngcbn.databinding.ActivityBongBinding
import java.util.Locale
import kotlin.math.abs

class BongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBongBinding
    private val mWebView by lazy { binding.mainWebView }
    private val IS_FULLSCREEN_PREF = "is_fullscreen_pref"
    private var mLastTouch: Long = 0
    private val mTouchThreshold: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = ActivityBongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeWebView()

        applyFullScreen(isFullScreen())
        setupFullScreenToggle()

        mWebView.loadUrl("file:///android_asset/dist/index.html?lang=" + Locale.getDefault().language)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        val settings = mWebView.settings
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            databasePath = filesDir.parentFile?.path + "/databases"
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFullScreenToggle() {
        mWebView.setOnTouchListener { _, event ->
            val currentTime = System.currentTimeMillis()
            if (event.action == MotionEvent.ACTION_UP
                && abs(currentTime - mLastTouch) > mTouchThreshold
            ) {
                val toggledFullScreen = !isFullScreen()
                saveFullScreen(toggledFullScreen)
                applyFullScreen(toggledFullScreen)
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                mLastTouch = currentTime
            }
            false
        }
    }

    private fun saveFullScreen(isFullScreen: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen)
        editor.apply()
    }

    private fun isFullScreen(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            IS_FULLSCREEN_PREF,
            true
        )
    }

    private fun applyFullScreen(isFullScreen: Boolean) {
        if (isFullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        mWebView.destroy()
    }
}