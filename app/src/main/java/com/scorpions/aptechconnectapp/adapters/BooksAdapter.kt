package com.scorpions.aptechconnectapp.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.BookActivity
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.models.Book
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import kotlinx.android.synthetic.main.book_layout.view.*

class BooksAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val bookCell = layoutInflater.inflate(R.layout.book_layout, parent, false)
        utils = Utils(parent.context)
        return BookViewHolder(bookCell)
    }

    override fun getItemCount(): Int {
        return this.books.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = this.books[position]
        holder.view.userName.text = book.title
        holder.view.userCourse.text = "Written By ${book.author}"
        holder.view.userBranch.text = book.user.name

        holder.book = book
    }

}


class BookViewHolder(val view: View, var book: Book? = null): RecyclerView.ViewHolder(view){

    companion object {
        const val BOOK_OBJECT = "BOOK_OBJECT"
    }

    private fun navigateToSingleBook(){

        val intent = Intent(view.context, BookActivity::class.java)

        val jsonGSON = Gson()
        val bookOBJECT = jsonGSON.toJson(book)
        intent.putExtra(BOOK_OBJECT, bookOBJECT)

        view.context.startActivity(intent)
    }

    init {
        view.setOnClickListener {
            this.navigateToSingleBook()
        }
    }

}