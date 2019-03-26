package com.scorpions.aptechconnectapp.models

class Book(
    var id: Int,
    var user: User,
    var title: String,
    var author: String,
    var book: String,
    var is_link: Boolean,
    var created_at: String,
    var updated_at: String
){}