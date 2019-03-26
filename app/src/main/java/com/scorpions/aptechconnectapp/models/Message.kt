package com.scorpions.aptechconnectapp.models

class Message(
    var id: Int,
    var user: User,
    var receiver: User,
    var message: String,
    var image: String,
    var created_at: String,
    var updated_at: String
){}