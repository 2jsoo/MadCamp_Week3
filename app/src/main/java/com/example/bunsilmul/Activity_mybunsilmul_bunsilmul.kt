package com.example.bunsilmul

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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


class Activity_mybunsilmul_bunsilmul: AppCompatActivity() {

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
            val intent = Intent(this@Activity_mybunsilmul_bunsilmul, ChatActivity::class.java)

            objectid?.let{intent.putExtra("id", it)}
            category?.let{intent.putExtra("category", it)}
            information?.let{intent.putExtra("information", it)}
            objectuid?.let{intent.putExtra("uid",it)}

            startActivity(intent)

//            FirebaseDatabase.getInstance().reference.child(objectid).child("send").push().setValue(FirebaseAuth.getInstance().currentUser?.uid)
//            FirebaseDatabase.getInstance().reference.child(objectid).push().child("find").push().setValue(objectuid)
            //둘다 같은 값이 없는 경우에만 적용....
            if(objectuid != FirebaseAuth.getInstance().currentUser?.uid) {
                FirebaseDatabase.getInstance().reference.child(objectid).child("send").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var exist = true
                        for (i in snapshot.children) {
                            if (i.getValue() == FirebaseAuth.getInstance().currentUser?.uid) {
                                exist = false
                            }
                        }
                        if (exist) {
                            FirebaseDatabase.getInstance().reference.child(objectid).child("send").push().setValue(FirebaseAuth.getInstance().currentUser?.uid)
                        }

                    }
                })
            }

            FirebaseDatabase.getInstance().reference.child(objectid).child("find").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var exist = true
                    for (i in snapshot.children) {
                        if (i.getValue() == objectuid) {
                            exist = false
                        }
                    }
                    if (exist) {
                        FirebaseDatabase.getInstance().reference.child(objectid).child("find").push().setValue(objectuid)
                    }

                }
            })




//            if(FirebaseDatabase.getInstance().reference.child(objectid).child("send").orderByValue().equalTo(FirebaseAuth.getInstance().currentUser?.uid) == null){
//                FirebaseDatabase.getInstance().reference.child(objectid).child("send").push().setValue(FirebaseAuth.getInstance().currentUser?.uid)
//            }
//
//            if(FirebaseDatabase.getInstance().reference.child(objectid).push().child("find").orderByValue().equalTo(objectuid) == null){
//                FirebaseDatabase.getInstance().reference.child(objectid).push().child("find").push().setValue(objectuid)
//            }


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