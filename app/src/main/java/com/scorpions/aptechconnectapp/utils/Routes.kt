package com.scorpions.aptechconnectapp.utils

class Routes{
    val END_POINT: String = "https://aptechconnect.herokuapp.com/"
    val SLASH_END_POINT: String = "https://aptechconnect.herokuapp.com"

    //USER ROUTES
    val userLogin: String = "api/login/"
    val getUsers = "api/users/all/"
    val editProfileImage = "api/users/edit/image/add/"

    //ARTICLES
    val getArticles = "api/articles/all/"
    val postComment = "api/articles/comment/add/"
    val postArticle = "api/articles/new/"

    //EVENT
    val getEvents = "api/events/all/"

    //BOOK
    val getBooks = "api/books/all/"

    //FEEDBACK
    val sendFeedback = "api/feedback/send/"
}