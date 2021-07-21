package com.neobns.lifecyclejs.presentation

data class JsConfirmData(val title: String,
                         val message: String,
                         val okBtnTitle: String = "확인",
                         val cancelBtnTitle: String = "취소")