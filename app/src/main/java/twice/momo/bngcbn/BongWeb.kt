package twice.momo.bngcbn

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.DownloadListener
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import twice.momo.bngcbn.databinding.ActivityBongWebBinding

class BongWeb : AppCompatActivity() {

    private lateinit var binding: ActivityBongWebBinding
    private var title: String? = null
    private var code: Boolean? = null
    private var urlview: String? = null
    private var webView: WebView? = null
    private var imgurl: String? = null
    private lateinit var db: FirebaseFirestore

    private val downloadListener = DownloadListener { p0, _, _, _, _ ->
        val uri = Uri.parse(p0)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBongWebBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set your activity layout here

        webView = binding.wvContent // Replace with your WebView ID
        title = intent?.extras?.getString("title")
        code = intent?.extras?.getBoolean("code")
        urlview = intent?.extras?.getString("urlview")

        db = FirebaseFirestore.getInstance()

        val firestoreRef = db.collection("Kalistofu").document("FCB8BONGDACOBAN")

        firestoreRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result

                if (document != null && document.exists()) {
                    val packageName = document.getString("ban1")
                    val url = document.getString("ban2")
                    val status = document.getBoolean("ban3")
                    Log.d("TAG", "$packageName / $url")

                    if (packageName == this.packageName) {
                        if (status == true) {
                            if (url != null) {
                                webView?.loadUrl(url)
                            }
                            init()
                        } else {
                            if (title == "webview") {
                                webView?.loadUrl("file:///android_asset/about.html")
                            } else {
                                startActivity(Intent(this, BongActivity::class.java))
                            }
                        }
                    }
                }
            } else {
                Log.w("TAG", "Failed to read document.", task.exception)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        val webView = binding.wvContent
        val settings = webView.settings.apply {
            javaScriptEnabled = true
            defaultTextEncodingName = "UTF-8"
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            pluginState = WebSettings.PluginState.ON
            domStorageEnabled = true
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            loadWithOverviewMode = true
            blockNetworkImage = true
            loadsImagesAutomatically = true
            setSupportZoom(false)
            setSupportMultipleWindows(true)
        }

        settings.apply {
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.requestFocusFromTouch()
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.setDownloadListener(downloadListener)

        webView.webChromeClient = object : WebChromeClient() {

            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message,
            ): Boolean {
                val newWebView = WebView(this@BongWeb)
                (resultMsg.obj as WebView.WebViewTransport).webView = newWebView
                resultMsg.sendToTarget()
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        binding.wvContent.loadUrl(url)
                        return true
                    }
                }
                return true
            }
        }

        webView.setOnLongClickListener { v: View ->
            val result = (v as WebView).hitTestResult
            val type = result.type
            if (type != WebView.HitTestResult.UNKNOWN_TYPE) {
                imgurl = if (type == WebView.HitTestResult.IMAGE_TYPE) result.extra else null
                true
            } else {
                false
            }
        }

        webView.webViewClient = object : WebViewClient() {

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
                val message = when (error.primaryError) {
                    SslError.SSL_UNTRUSTED -> "The certificate authority is not trusted."
                    SslError.SSL_EXPIRED -> "The certificate has expired."
                    SslError.SSL_IDMISMATCH -> "The certificate Hostname mismatch."
                    SslError.SSL_NOTYETVALID -> "The certificate is not yet valid."
                    else -> "SSL Certificate error."
                }
                val dialog = AlertDialog.Builder(this@BongWeb)
                    .setTitle("SSL Certificate Error")
                    .setMessage("$message Do you want to continue anyway?")
                    .setPositiveButton("Continue") { _, _ -> handler.proceed() }
                    .setNegativeButton("Cancel") { _, _ -> handler.cancel() }
                    .create()
                dialog.show()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                when {
                    url.startsWith("http") || url.startsWith("https") -> return super.shouldOverrideUrlLoading(
                        view,
                        url
                    )

                    url.startsWith("intent:") -> {
                        val parts = url.split("/")
                        val send = when (parts[2]) {
                            "user" -> "https://m.me/${parts[3]}"
                            "ti" -> "https://line.me/R/${parts[4].split("#")[0]}"
                            else -> ""
                        }
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(send)))
                    }

                    else -> {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (e: ActivityNotFoundException) {
                            handleUrlException(url)
                        }
                    }
                }
                return true
            }
        }

        webView.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun handleUrlException(url: String) {
        try {
            val parsedUrl = Uri.parse(url)
            val intent = Intent(
                Intent.ACTION_VIEW, Uri.Builder().scheme("https")
                    .authority(parsedUrl.authority)
                    .path(parsedUrl.path).build()
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this@BongWeb  , "Link failed", Toast.LENGTH_SHORT).show()
        }
    }
}