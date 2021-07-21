package com.neobns.lifecyclejs.presentation

import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.webkit.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.and.base.common.EventObserver
import com.and.base.log.Log
import com.and.base.ui.BaseActivity
import com.neobns.lifecyclejs.BuildConfig
import com.neobns.lifecyclejs.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override val vm by viewModels<MainViewModel>()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.vm = vm
        binding.web.run {
            settings.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                javaScriptEnabled = true
                setSupportMultipleWindows(true)
                domStorageEnabled = true
                databaseEnabled = true
                textZoom = 100
//                userAgentString = PP.userAgent
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webChromeClient = CChromeClient()
            webViewClient = CWebViewClient()
        }
    }

    override fun getDialog(
        title: Any?,
        message: Any?,
        view: View?,
        positiveButtonText: Any?,
        positiveListener: ((dialogInterface: DialogInterface, position: Int) -> Unit)?,
        negativeButtonText: Any?,
        negativeListener: ((dialogInterface: DialogInterface, position: Int) -> Unit)?,
        neutralButtonText: Any?,
        neutralListener: ((dialogInterface: DialogInterface, position: Int) -> Unit)?
    ): AlertDialog? {
        return super.getDialog(
            title,
            message,
            view,
            positiveButtonText,
            positiveListener,
            negativeButtonText,
            negativeListener,
            neutralButtonText,
            neutralListener
        )?.also {
            it.setCanceledOnTouchOutside(false)
            it.setCancelable(false)
        }
    }

    override fun onLoadOnce() {
        super.onLoadOnce()

        setWebContentsDebuggingEnabled()

        vm.url.observe(this) { binding.web.loadUrl(it) }
        vm.jsAlert.observe(this) {
            it?.let { pair ->
                showDialog(
                    null,
                    pair.first.message,
                    null,
                    "확인",
                    { _, _ -> pair.second.confirm() })
            }
        }
        vm.jsConfirm.observe(this) {
            it?.let { pair ->
                showDialog(
                    null,
                    pair.first.message,
                    null,
                    pair.first.okBtnTitle,
                    { _, _ -> pair.second.confirm() },
                    pair.first.cancelBtnTitle,
                    { _, _ -> pair.second.cancel() })
            }
        }
        vm.errorAlert.observe(this) {
            it?.let { pair ->
                showDialog(
                    null,
                    pair.first,
                    null,
                    "확인",
                    { _, _ -> pair.second.confirm() })
            }
        }
        vm.startActivity.observe(this, EventObserver {
            startActivity(it)
        })
    }

    private fun setWebContentsDebuggingEnabled() {
        runCatching { WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG) }
            .onFailure { it.printStackTrace() }
    }

    override fun onPause() {
        sendJavascript("onPause()")
        super.onPause()
        binding.web.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.web.onResume()
        sendJavascript("onResume()")
    }

    private fun sendJavascript(script: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            binding.web.post { sendJavascript(script) }
            return
        }
        android.util.Log.w("sendJavascript", script)
        binding.web.evaluateJavascript(script, null)
    }

    inner class CWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return vm.shouldOverrideUrlLoading(url)
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            Log.i("OnPageStarted")
            vm.onPageStarted(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            Log.i("OnPageFinished")
            vm.onPageFinished(url)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            Log.i("onLoadResource : $url")
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            Log.i("shouldIntercept: ${request?.url}")
            return super.shouldInterceptRequest(view, request)
        }
    }

    inner class CChromeClient : WebChromeClient() {
        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            return vm.onJsAlert(message, result)
        }

        override fun onJsConfirm(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {
            return vm.onJsConfirm(message, result)
        }
    }
}