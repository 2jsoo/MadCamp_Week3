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

class Activity_mychat: AppCompatActivity() {

    lateinit var mmychatAdapter: mychat_adapter
    lateinit var mmychat : RecyclerView

    var database = FirebaseDatabase.getInstance()
    var databaseReference = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mychat)
        mmychatAdapter = mychat_adapter(this@Activity_mychat)
        mmychat = findViewById(R.id.mychat_recycler_view)
        val layout = LinearLayoutManager(this)
        mmychat.layoutManager = layout
        mmychat.adapter = mmychatAdapter
        mmychatAdapter.bindItem(mutableListOf<myChat>())

        FirebaseDatabase.getInstance().reference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TOTAL",""+snapshot.getValue())
                for ( chatroom in snapshot.children) {
                    Log.d("mychatchagne", "" + chatroom.key)
                    for ( finduid in chatroom.child("find").children){
                        Log.d("finduid", "" + finduid.getValue())
                        if(FirebaseAuth.getInstance().currentUser?.uid == finduid.getValue()){
                            mmychatAdapter.addItem(myChat(""+chatroom.key,true)) // chatroom은 child 이름 안들어있음...
                        }
                    }
                    for ( senduid in chatroom.child("send").children){
                        Log.d("senduid", "" + senduid.getValue())
                        if(FirebaseAuth.getInstance().currentUser?.uid == senduid.getValue()){
                            mmychatAdapter.addItem(myChat(""+chatroom.key,false))
                        }


                    }
                }
            }

        })

    }

}