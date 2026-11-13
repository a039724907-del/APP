package com.offlinephp.studio

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(243, 246, 252)
        window.navigationBarColor = Color.rgb(243, 246, 252)

        web = WebView(this)
        web.setBackgroundColor(Color.rgb(243, 246, 252))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowContentAccess = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(PhpStudioBridge(), "PHPStudio")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class PhpStudioBridge {
        @JavascriptInterface
        fun saveTextFile(requestedName: String, content: String) {
            try {
                val safeName = requestedName
                    .substringAfterLast('/')
                    .substringAfterLast('\\')
                    .replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .ifBlank { "php-code.php" }
                val bytes = content.toByteArray(StandardCharsets.UTF_8)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PHP Studio")
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw IllegalStateException("לא ניתן ליצור קובץ בתיקיית ההורדות")
                    contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        ?: throw IllegalStateException("לא ניתן לכתוב את הקובץ")
                } else {
                    val directory = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "PHP Studio")
                    if (!directory.exists() && !directory.mkdirs()) {
                        throw IllegalStateException("לא ניתן ליצור תיקיית הורדות")
                    }
                    FileOutputStream(File(directory, safeName)).use { it.write(bytes) }
                }

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "הקובץ נשמר בתיקיית ההורדות: $safeName", Toast.LENGTH_LONG).show()
                }
            } catch (error: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "שמירת הקובץ נכשלה: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.removeJavascriptInterface("PHPStudio")
            web.destroy()
        }
        super.onDestroy()
    }
}