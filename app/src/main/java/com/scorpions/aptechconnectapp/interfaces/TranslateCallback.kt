package com.scorpions.aptechconnectapp.interfaces

internal interface TranslateCallback {
    fun onSuccess(translatedText: String)
    fun onFailure()
}