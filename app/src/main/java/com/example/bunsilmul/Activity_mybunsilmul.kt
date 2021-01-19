package com.example.bunsilmul

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import retrofit2.Call

class Activity_mybunsilmul: AppCompatActivity() {

    lateinit var mmybunsilmuladapter: mybunsilmul_adapter
    lateinit var mmybunsilmul : RecyclerView

    var database = FirebaseDatabase.getInstance()
    var databaseReference = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mybunsilmul)
        mmybunsilmuladapter = mybunsilmul_adapter(this@Activity_mybunsilmul)
        mmybunsilmul = findViewById(R.id.mybunsilmul_recycler_view)
        val layout = LinearLayoutManager(this)
        mmybunsilmul.layoutManager = layout
        mmybunsilmul.adapter = mmybunsilmuladapter
        mmybunsilmuladapter.bindItem(arrayOf<bunsilmul>())

        FirebaseAuth.getInstance().currentUser?.let{
            val call = BunsilmulApiObject.retrofitService.GetUserBunsilmul(it.uid)
            call.enqueue(object: retrofit2.Callback<Array<bunsilmul>> {
                override fun onFailure(call: Call<Array<bunsilmul>>, t: Throwable) {
                    Log.d("BunsilmulCreate", "Fail")
                }

                override fun onResponse(call: Call<Array<bunsilmul>>, response: retrofit2.Response<Array<bunsilmul>>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            mmybunsilmuladapter.bindItem(it)
                        }
                    } else {
                        Log.d("BunsilmulCreate", "response is error")
                    }
                }
            })
        }

    }

}