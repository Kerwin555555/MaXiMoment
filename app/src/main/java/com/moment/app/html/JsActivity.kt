package com.moment.app.html

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import com.didi.drouter.annotation.Router
import com.moment.app.databinding.ActivityHtmlBinding
import com.moment.app.utils.BaseActivity
import im.delight.android.webview.AdvancedWebView
import okio.Buffer


@Router(scheme = ".*", host = ".*", path = "/html")
class JsActivity : BaseActivity(), AdvancedWebView.Listener {
    companion object {
        val TYPE_GET = 0
        val TYPE_POST = 1
    }
    private lateinit var binding: ActivityHtmlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHtmlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webview.setGeolocationEnabled(true)
        binding.webview.setListener(this, this)
        binding.webview.setMixedContentAllowed(false)

        val url = intent.getStringExtra("url")
        if (!TextUtils.isEmpty(url)) {
            if (intent.getIntExtra("type", TYPE_GET) == TYPE_POST) {
                binding.webview.postUrl(url!!, Buffer().readByteArray())
            } else {
                binding.webview.loadUrl(url, true)
            }
        }
    }


    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        binding.webview.reload()
        
    }

    @SuppressLint("NewApi")
    override fun onPause() {
        binding.webview.onPause()
        
        super.onPause()
    }

    override fun onDestroy() {
        binding.webview.onDestroy()
        
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        binding.webview.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        if (!binding.webview.onBackPressed()) {
            return
        }
        
        super.onBackPressed()
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {}

    override fun onPageFinished(url: String?) {

    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {

    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {}
}

