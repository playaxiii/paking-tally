package com.axiii.parkingtally.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.axiii.parkingtally.R
import com.axiii.parkingtally.databinding.ActivityMainBinding
import com.axiii.parkingtally.model.JavaScriptInterface

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val webView = binding.webView

        // Enable JavaScript
        val webSettings: WebSettings = webView.settings
        webSettings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
        }


        // set a WebViewClient to handle page navigation
        webView.webViewClient = WebViewClient()


        // load URL
        webView.loadUrl("https://park-98428.web.app/login")

        // add JavaScriptInterface to webView
        val jsInterface = JavaScriptInterface(this)
        webView.addJavascriptInterface(jsInterface, "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.pgBar.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pgBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }

        // download listener and blob to b64
        webView.setDownloadListener(DownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, contentLength ->
            try {
                if (downloadUrl.startsWith("blob:")) {
                    // Handle blob URL
                    webView.loadUrl(
                        JavaScriptInterface.getBase64StringFromBlobUrl(
                            downloadUrl,
                            mimetype
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error handling download", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    // Override onBackPressed to handle webView navigation
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override

            fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}