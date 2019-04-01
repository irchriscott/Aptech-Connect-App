package com.scorpions.aptechconnectapp

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.adapters.BookViewHolder
import com.scorpions.aptechconnectapp.models.Book
import com.scorpions.aptechconnectapp.utils.Routes
import kotlinx.android.synthetic.main.activity_book.*

class BookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        val gson = Gson()

        val bookString = intent.getStringExtra(BookViewHolder.BOOK_OBJECT)
        val book: Book = gson.fromJson(bookString, Book::class.java)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = book.title

        val routes = Routes()

        pdfView.fromUri(Uri.parse("${routes.SLASH_END_POINT}${book.book}")).load()
    }
}
