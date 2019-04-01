package com.scorpions.aptechconnectapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_article.*
import com.esafirm.imagepicker.features.ImagePicker
import com.google.gson.Gson
import com.scorpions.aptechconnectapp.models.ServerResponse
import com.scorpions.aptechconnectapp.models.User
import com.scorpions.aptechconnectapp.providers.UserAuthentication
import com.scorpions.aptechconnectapp.utils.Routes
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.MultipartBody


class AddArticle : AppCompatActivity() {

    private var imageToUpload: File? = null
    private lateinit var userAuthentication: UserAuthentication
    private val routes: Routes = Routes()
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        supportActionBar!!.title = "Add Article"
        userAuthentication = UserAuthentication(this@AddArticle)
        user = userAuthentication.getUserFromSharedPreferences()

        addImages.setOnClickListener {
            ImagePicker.create(this@AddArticle).single().start(0)
        }

        submit.setOnClickListener {
            this.postArticle(user)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null){
            val images = ImagePicker.getImages(data)
            if(images.isNotEmpty()){
                val image = images[0]
                this.imageToUpload = File(images[0].path)
                Picasso.get().load(File(image.path)).into(articleImage)
            }
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun postArticle(user: User?) {
        val url = "${routes.END_POINT}${routes.postArticle}${user?.token}/"

        val jsonObject = JSONObject()
        val mediaType = MediaType.parse("application/json")
        val client = OkHttpClient()

        val MEDIA_TYPE_PNG = MediaType.parse("image/png")

        if(this.imageToUpload != null && titleText.text.toString() != "" && textText.text.toString() != "") {

            val req = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("title", titleText.text.toString())
                .addFormDataPart("text", textText.text.toString())
                .addFormDataPart("image", "image.png", RequestBody.create(MEDIA_TYPE_PNG, this.imageToUpload)).build()

            val request = Request.Builder()
                .url(url)
                .post(req)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@AddArticle, e.message, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body()!!.string()
                    val gson = Gson()
                    val serverResponse = gson.fromJson(responseBody, ServerResponse::class.java)
                    if (response.code() == 200) {
                        runOnUiThread {
                            Toast.makeText(this@AddArticle, serverResponse.message, Toast.LENGTH_SHORT).show().also {
                                startActivity(Intent(this@AddArticle, MainActivity::class.java))
                            }
                        }
                    } else {
                        runOnUiThread {
                            val alertDialogBuilder = AlertDialog.Builder(this@AddArticle)
                            alertDialogBuilder.setTitle(serverResponse.type.capitalize())
                            alertDialogBuilder.setMessage(serverResponse.message)
                            alertDialogBuilder.setNegativeButton("OK", DialogInterface.OnClickListener { _, _ ->
                                Toast.makeText(this@AddArticle, "Enter Valid Data", Toast.LENGTH_LONG).show()
                            })
                            alertDialogBuilder.setOnCancelListener {
                                Toast.makeText(this@AddArticle, "Enter Valid Data", Toast.LENGTH_LONG).show()
                            }
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        }
                    }
                }

            })
        } else {
            Toast.makeText(this@AddArticle, "Fill All Fields", Toast.LENGTH_LONG).show()
        }
    }
}
