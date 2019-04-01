package com.scorpions.aptechconnectapp.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.StudentActivity
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.student_layout.view.*

class StudentsAdapter(private val students: List<User>) : RecyclerView.Adapter<StudentViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val studentCell = layoutInflater.inflate(R.layout.student_layout, parent, false)
        utils = Utils(parent.context)
        return StudentViewHolder(studentCell)
    }

    override fun getItemCount(): Int {
        return this.students.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = this.students[position]
        holder.view.userName.text = student.name
        holder.view.userCourse.text = student.course.name
        holder.view.userBranch.text = "${student.branch.town}, ${student.branch.country}"

        holder.student = student

        Picasso.get().load("${this.routes.SLASH_END_POINT}${student.image}").into(holder.view.userImage)
    }

}


class StudentViewHolder(val view: View, var student: User? = null) : RecyclerView.ViewHolder(view){

    companion object {
        const val STUDENT_OBJECT = "STUDENT_OBJECT"
    }

    private fun navigateToSingleStudent(){

        val intent = Intent(view.context, StudentActivity::class.java)

        val jsonGSON = Gson()
        val articleOBJECT = jsonGSON.toJson(student)
        intent.putExtra(STUDENT_OBJECT, articleOBJECT)

        view.context.startActivity(intent)
    }

    init {
        view.setOnClickListener {
            this.navigateToSingleStudent()
        }
    }
}