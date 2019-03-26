package com.scorpions.aptechconnectapp.models

class Comment(
    var id: Int,
    var user: User,
    var comment: String,
    var created_at: String,
    var updated_at: String
){}