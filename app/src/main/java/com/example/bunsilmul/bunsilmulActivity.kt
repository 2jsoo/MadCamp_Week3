package com.example.bunsilmul

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream
import java.util.*

private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.249.18.133:8080/") // 마지막 / 반드시 들어가야 함
    .addConverterFactory(GsonConverterFactory.create()) // converter 지정
    .build() // com.example.bunsilmul.retrofit 객체 생성

data class bunsilmulphoto(
    var photo: String
)

class bunsilmulActivity: AppCompatActivity() {

//    val objectid: String = intent.getStringExtra("id")
//    val category: String = intent.getStringExtra("category")
//    val information: String = intent.getStringExtra("information")

    lateinit var objectid: String
    lateinit var category: String
    lateinit var information: String
    lateinit var objectuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra("id")?.let{objectid = it}
        intent.getStringExtra("category")?.let{category = it}
        intent.getStringExtra("information")?.let{information = it}
        intent.getStringExtra("uid")?.let{objectuid = it}


        setContentView(R.layout.activity_bunsilmul)
        val photo = findViewById<ImageView>(R.id.bunsilmul_photo)
        val text_category = findViewById<TextView>(R.id.bunsilmul_text_category)
        val text_information = findViewById<TextView>(R.id.bunsilmul_text_information)

//        text_category.text = intent.getStringExtra("category")
//        text_information.text = intent.getStringExtra("information")

        text_category.text = category
        text_information.text = information



        intent.getStringExtra("id")?.let {
            val call = BunsilmulApiObject.retrofitService.GetBunsilmulPhoto(it)
            call.enqueue(object : retrofit2.Callback<bunsilmulphoto> {
                override fun onFailure(call: Call<bunsilmulphoto>, t: Throwable) {
                    Log.d("BunsilmulCreate", "Fail")
                }

                override fun onResponse(
                    call: Call<bunsilmulphoto>,
                    response: retrofit2.Response<bunsilmulphoto>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            photo.setImageBitmap(stringToImage(it.photo))
                        }
                    } else {
                        Log.d("BunsilmulCreate", "response is error")
                    }
                }
            })
        }

        val chat_button = findViewById<Button>(R.id.chat_button)
        if(FirebaseAuth.getInstance().uid == intent.getStringExtra("uid")){
//            chat_button.isEnabled = false
        }else{
            chat_button.isEnabled = true
        }

        chat_button.setOnClickListener {
            val intent = Intent(this@bunsilmulActivity, ChatActivity::class.java)

            objectid?.let{intent.putExtra("id", it)}
            category?.let{intent.putExtra("category", it)}
            information?.let{intent.putExtra("information", it)}
            objectuid?.let{intent.putExtra("uid",it)}

            startActivity(intent)
        }

    }

    private fun stringToImage(string: String): Bitmap {
        //Base64String 형태를 ByteArray로 풀어줘야 한다
        val data: String = string
        //데이터 base64 형식으로 Decode
        val txtPlainOrg = ""
        val bytePlainOrg = Base64.decode(data, 0)
        //byte[] 데이터  stream 데이터로 변환 후 bitmapFactory로 이미지 생성
        val inStream = ByteArrayInputStream(bytePlainOrg)
        val bm = BitmapFactory.decodeStream(inStream)
        return bm
    }
}