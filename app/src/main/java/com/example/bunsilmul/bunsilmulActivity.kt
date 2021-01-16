package com.example.bunsilmul

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    .build() // retrofit 객체 생성

data class bunsilmulphoto(
    var photo: String
)

class bunsilmulActivity: AppCompatActivity() {

//    val objectid = intent.getStringExtra("id")
//    val category = intent.getStringExtra("category")
//    val information = intent.getStringExtra("information")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bunsilmul)
        val photo = findViewById<ImageView>(R.id.bunsilmul_photo)
        val text_category = findViewById<TextView>(R.id.bunsilmul_text_category)
        val text_information = findViewById<TextView>(R.id.bunsilmul_text_information)

        text_category.text = intent.getStringExtra("category")
        text_information.text = intent.getStringExtra("information")

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