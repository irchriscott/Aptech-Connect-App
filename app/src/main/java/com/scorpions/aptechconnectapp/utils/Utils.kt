package com.scorpions.aptechconnectapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.scorpions.aptechconnectapp.models.Language
import java.io.InputStream
import org.joda.time.format.DateTimeFormat
import android.app.Activity
import android.database.Cursor


class Utils(
    private val contxt: Context
){
    private val context: Context = this.contxt

    public fun translate(textToTranslate: String, targetLanguage: String): String? {
        return try {
            val options: TranslateOptions = TranslateOptions.newBuilder().setApiKey("AIzaSyD2mLGusTJZqu7zesBgobnoVIzN6hIayvk").build()
            val translate = options.service
            val language = translate.detect(textToTranslate)
            val translation = translate.translate(textToTranslate,
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage("fr")
            )

            translation.translatedText

        } catch(e: Exception) {
            e.message
        }
    }

    private fun loadJSONLanguages() : String{
        return context.assets.open("languages.json").bufferedReader().use {
            it.readText()
        }
    }

    public fun getLanguages(): List<Language> {
        val gson = GsonBuilder().create()
        return gson.fromJson<List<Language>>(this.loadJSONLanguages(), object : TypeToken<List<Language>>(){}.type)
    }

    public fun formatDateTime(dateTime: String) : String{
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parseDateTime(dateTime)
        val fmt = DateTimeFormat.forPattern("d MMMM, yyyy 'at' H:m a")
        return date.toString(fmt)
    }

    public fun formatDate(dateD: String): String{
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val date = formatter.parseDateTime(dateD)
        val fmt = DateTimeFormat.forPattern("d MMMM, yyyy")
        return date.toString(fmt)
    }

    public fun dateTimeToMillis(dateTime: String): Long{
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val date = formatter.parseDateTime(dateTime)
        return  date.millis
    }

    public fun getCalendarUriBase(act: Activity): String? {

        var calendarUriBase: String? = null
        var calendars = Uri.parse("content://calendar/calendars")
        var managedCursor: Cursor? = null
        try {
            managedCursor = act.managedQuery(calendars, null, null, null, null)
        } catch (e: Exception) { }

        if (managedCursor != null) {
            calendarUriBase = "content://calendar/"
        } else {
            calendars = Uri.parse("content://com.android.calendar/calendars")
            try {
                managedCursor = act.managedQuery(calendars, null, null, null, null)
            } catch (e: Exception) { }

            if (managedCursor != null) {
                calendarUriBase = "content://com.android.calendar/"
            }
        }
        return calendarUriBase
    }

}