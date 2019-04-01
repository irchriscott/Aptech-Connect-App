package com.scorpions.aptechconnectapp.models

class Article(
    var id: Int,
    var user: User,
    var uuid: String,
    var title: String,
    var text: String,
    var image: String,
    var created_at: String,
    var updated_at: String,
    var comments: MutableList<Comment>
){}