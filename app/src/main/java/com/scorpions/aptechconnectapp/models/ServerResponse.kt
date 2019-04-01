package com.scorpions.aptechconnectapp.models

class ServerResponse(
    var type: String,
    var message: String,
    var status: Int,
    var msgs: List<String>
){}