package com.scorpions.aptechconnectapp.models

class User(
    var id: Int,
    var name: String,
    var email: String,
    var gender: String,
    var dob: String,
    var image: String,
    var user_type: String,
    var roll_no: String,
    var batch_no: String,
    var course: Course,
    var branch: Branch,
    var token: String,
    var created_at: String,
    var updated_at: String
){

}