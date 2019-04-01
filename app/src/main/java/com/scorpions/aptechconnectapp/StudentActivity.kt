package com.scorpions.aptechconnectapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.esafirm.imagepicker.features.ImagePicker
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.adapters.StudentViewHolder
import com.scorpions.aptechconnectapp.models.ServerResponse
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_student.*
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

class StudentActivity : AppCompatActivity() {

    private lateinit var userAuthentication: UserAuthentication
    private lateinit var utils: Utils
    private val routes: Routes = Routes()

    companion object {
        const val STUDENT_OBJECT = "STUDENT_OBJECT"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        userAuthentication = UserAuthentication(this@StudentActivity)
        val user = userAuthentication.getUserFromSharedPreferences()
        utils = Utils(this@StudentActivity)

        val gson = Gson()

        val studentString = intent.getStringExtra(StudentViewHolder.STUDENT_OBJECT)
        val student: User = gson.fromJson(studentString, User::class.java)

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.title = student.name

        name.text = student.name
        email.text = student.email
        roll_no.text = student.roll_no.toUpperCase()
        batch_no.text = student.batch_no.toUpperCase()
        location.text = "${student.branch.town}, ${student.branch.country}"
        branch.text = student.course.name
        dob.text = utils.formatDate(student.dob)
        gender.text = student.gender

        Picasso.get().load("${this.routes.SLASH_END_POINT}${student.image}").into(image)

        if(user?.id != student.id){
            editPicture.visibility = View.GONE
            editPicture.isActivated = false
            editPicture.isClickable = false
        } else {
            editPicture.setOnClickListener {
                ImagePicker.create(this@StudentActivity).single().start(0)
            }
        }

        openChat.setOnClickListener {
            if(user?.id != student.id){
                val intent = Intent(this@StudentActivity, ChatActivity::class.java)

                val jsonGSON = Gson()
                val articleOBJECT = jsonGSON.toJson(student)
                intent.putExtra(STUDENT_OBJECT, articleOBJECT)

                startActivity(intent)
            } else {
                Toast.makeText(this@StudentActivity, "Cannot Chat With Current Session", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null){
            val images = ImagePicker.getImages(data)
            if(images.isNotEmpty()){
                val image = File(images[0].path)
                this.editProfileImage(image)
            }
        }
    }

    private fun editProfileImage(img: File){

        val user = userAuthentication.getUserFromSharedPreferences()

        val url = "${routes.END_POINT}${routes.editProfileImage}${user?.token}/"

        val jsonObject = JSONObject()
        val mediaType = MediaType.parse("application/json")
        val client = OkHttpClient()

        val MEDIA_TYPE_PNG = MediaType.parse("image/png")

        val req = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.png", RequestBody.create(MEDIA_TYPE_PNG, img)).build()

        val request = Request.Builder()
            .url(url)
            .post(req)
            .build()

        Toast.makeText(this@StudentActivity, "Image Uploading ...", Toast.LENGTH_LONG).show()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@StudentActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()!!.string()
                val gson = Gson()
                if (response.code() == 200) {
                    val auth = gson.fromJson(responseBody, User::class.java)
                    userAuthentication.saveUserToSharedPreferences(auth)
                    runOnUiThread {
                        Toast.makeText(this@StudentActivity, "Image Updated Successfully", Toast.LENGTH_SHORT).show().also {
                            Picasso.get().load(img).into(image)
                        }
                    }
                } else {
                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                    runOnUiThread {
                        val alertDialogBuilder = AlertDialog.Builder(this@StudentActivity)
                        alertDialogBuilder.setTitle(serverResponse.type.capitalize())
                        alertDialogBuilder.setMessage(serverResponse.message)
                        alertDialogBuilder.setNegativeButton("OK", DialogInterface.OnClickListener { _, _ ->
                            Toast.makeText(this@StudentActivity, "Enter Valid Data", Toast.LENGTH_LONG).show()
                        })
                        alertDialogBuilder.setOnCancelListener {
                            Toast.makeText(this@StudentActivity, "Enter Valid Data", Toast.LENGTH_LONG).show()
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                }
            }

        })
    }
}
