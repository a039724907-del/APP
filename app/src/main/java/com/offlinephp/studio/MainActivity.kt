package com.offlinephp.studio
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import android.graphics.Color
class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  window.statusBarColor = Color.rgb(243,246,252)
  window.navigationBarColor = Color.rgb(243,246,252)
  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
  val web = WebView(this)
  web.setBackgroundColor(Color.rgb(243,246,252))
  web.settings.javaScriptEnabled = true
  web.settings.domStorageEnabled = true
  web.settings.allowFileAccess = true
  web.settings.allowContentAccess = true
  web.webViewClient = WebViewClient()
  web.webChromeClient = WebChromeClient()
  setContentView(web)
  web.loadUrl("file:///android_asset/index.html")
 }
 @Deprecated("Deprecated in Android")
 override fun onBackPressed() {
  val root = window.decorView.findViewById<View>(android.R.id.content) as? android.view.ViewGroup
  val web = root?.getChildAt(0) as? WebView
  if (web?.canGoBack() == true) web.goBack() else super.onBackPressed()
 }
}