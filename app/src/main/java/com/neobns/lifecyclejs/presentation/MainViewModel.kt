package com.neobns.lifecyclejs.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.JsResult
import android.widget.TextView
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.and.base.common.Event
import com.and.base.ui.BaseViewModel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import javax.inject.Inject

class MainViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : BaseViewModel() {

    private val initialUrl = "file:///android_asset/index.html"
    var inputUrl = ObservableField<String>(initialUrl)

    private val _url = MutableLiveData("")
    val url: LiveData<String> get() = _url

    private val _startActivity = MutableLiveData<Event<Intent>>()
    val startActivity: LiveData<Event<Intent>> get() = _startActivity

    private val _jsAlert = MutableLiveData<Pair<JsAlertData, JsResult>>()
    val jsAlert: LiveData<Pair<JsAlertData, JsResult>> get() = _jsAlert

    private val _jsConfirm = MutableLiveData<Pair<JsConfirmData, JsResult>>()
    val jsConfirm: LiveData<Pair<JsConfirmData, JsResult>> get() = _jsConfirm

    private val _errorAlert = MutableLiveData<Pair<String, JsResult>>()
    val errorAlert: LiveData<Pair<String, JsResult>> get() = _errorAlert

    fun onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
            _url.value = view.text.toString()
            return true
        }
        return false
    }

    fun onFocusListener(view: View, hasFocus: Boolean) {
        if (hasFocus && view is TextView && view.text.toString() == initialUrl)
            inputUrl.set("")
    }

    init {
        loadUrl()
    }

    fun loadUrl() {
        _url.value = inputUrl.get()
    }

    fun onPageStarted(url: String) {
        _isProgress.value = true
    }

    fun onPageFinished(url: String) {
        _isProgress.value = false
    }

    fun onJsAlert(message: String, jsResult: JsResult): Boolean {
        runCatching {
            val jsAlertTypeToken = object : TypeToken<JsAlertData>() {}.type
            val jsAlertData = Gson().fromJson<JsAlertData>(message, jsAlertTypeToken)
            _jsAlert.value = Pair(jsAlertData, jsResult)
        }.onFailure {
            _errorAlert.value = Pair(message, jsResult)
        }
        return true
    }

    fun onJsConfirm(message: String, jsResult: JsResult): Boolean {
        runCatching {
            val jsConfirmDataTypeToken = object : TypeToken<JsConfirmData>() {}.type
            val jsConfirmData = Gson().fromJson<JsConfirmData>(message, jsConfirmDataTypeToken)
            _jsConfirm.value = Pair(jsConfirmData, jsResult)
        }.onFailure {
            _errorAlert.value = Pair(message, jsResult)
        }
        return true
    }

    fun shouldOverrideUrlLoading(url: String): Boolean {
        if (url.isEmpty())
            return false

        if (url.startsWith("tel:")) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            _startActivity.value = Event(intent)
            return true
        }

        if (url.startsWith("android-app:") || url.startsWith("intent:") || url.startsWith("#Intent;")) {
            runCatching {
                val intent = Intent.parseUri(url, 0)
                val browserId = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)
                if (intent.action != null && intent.action == Intent.ACTION_VIEW && browserId.isNullOrEmpty())
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, "webapp")
                _startActivity.value = Event(intent)
                return true
            }.onFailure {
                it.printStackTrace()
                return true
            }
        }

        if (isInternal(url)) {
            _url.value = url
            return true
        }
        return false
    }

    private fun isInternal(url: String): Boolean {
        return url.matches("^(https?)://.*".toRegex())
    }
}