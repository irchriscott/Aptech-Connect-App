package com.scorpions.aptechconnectapp.adapters

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.models.Comment
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_layout.view.*

class CommentsAdapter(private val comments: MutableList<Comment>) : RecyclerView.Adapter<CommentViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val commentCell = layoutInflater.inflate(R.layout.comment_layout, parent, false)
        utils = Utils(parent.context)
        return CommentViewHolder(commentCell)
    }

    override fun getItemCount(): Int {
        return this.comments.count()
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.view.commentText.text = comment.comment
        holder.view.commentUserName.text = comment.user.name
        holder.view.postedAt.text = utils.formatDateTime(comment.created_at)

        Picasso.get().load("${this.routes.SLASH_END_POINT}${comment.user.image}").into(holder.view.commentUserImage)
    }

}

class CommentViewHolder(var view: View) : RecyclerView.ViewHolder(view) {}