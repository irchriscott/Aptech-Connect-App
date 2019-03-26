package com.scorpions.aptechconnectapp.models

class Event(
    var id: Int,
    var user: User,
    var uuid: String,
    var date: String,
    var time: String,
    var name: String,
    var description: String,
    var image: String,
    var venue: String,
    var created_at: String,
    var updated_at: String
){}