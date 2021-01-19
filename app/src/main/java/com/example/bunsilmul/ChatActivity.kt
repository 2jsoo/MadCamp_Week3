package com.example.bunsilmul

import android.os.Bundle
import android.os.PersistableBundle
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


class ChatActivity: AppCompatActivity() {

    lateinit var myuid: String
    lateinit var bunsilmuluid: String
    lateinit var objectid: String

    lateinit var mChatAdapter: Chat_Adapter
    lateinit var mChat : RecyclerView

    var database = FirebaseDatabase.getInstance()
    var databaseReference = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mChatAdapter = Chat_Adapter()
        mChat = findViewById(R.id.chat_recycler_view)
        val layout = LinearLayoutManager(this)
        mChat.layoutManager = layout
        mChat.adapter = mChatAdapter
        mChatAdapter.bindItem(mutableListOf<Chat>())

        intent.getStringExtra("id")?.let{objectid = it}
        intent.getStringExtra("uid")?.let{bunsilmuluid = it}
        FirebaseAuth.getInstance().uid?.let{myuid = it}

//        if(databaseReference.orderByChild().equalTo(objectid)){ // if chatting room doesn't exist, make chatting room
//            Log.d("Firebase","No data")
//            databaseReference.push().setValue(myuid)
//        }else{ // else, load chat
//
//        }
//        databaseReference.setValue(objectid)

        Log.d("object_id",objectid)

//        databaseReference.child(objectid)

        databaseReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(objectid)){ // else, load chat that exist already
                    databaseReference.child(objectid).addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
//                                val chat = snapshot.getValue(Chat::class.java)
//                                chat?.let{
//                                    mChatAdapter.addItem(chat)
//                                    Log.d("DataChange", "ADD")
//                                    it.chat?.let{Log.d("DataChange",it)}
//                                }
                            }else{
                                Log.d("CHATTING","NOT EXIST")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }else{ // if chatting room doesn't exist, make chatting room
                    databaseReference.child(objectid)
                }
            }
        })

        val send_button = findViewById<Button>(R.id.send_button)
        val edit_text = findViewById<EditText>(R.id.chat_edittext)
        send_button.isEnabled = false

        edit_text.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s==null){
                    send_button.isEnabled = false
                }else if(s.isEmpty()){
                    send_button.isEnabled = false
                } else{
                    send_button.isEnabled = true
                }
                }
        })

        send_button.setOnClickListener {
            val newchat = Chat(myuid, edit_text.text.toString())
            databaseReference.child(objectid).child("chat").push().setValue(newchat)
            edit_text.text = null
        }

        databaseReference.child(objectid).child("chat").addChildEventListener(object: ChildEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val newchat = snapshot.getValue(Chat::class.java)
                newchat?.let{
                    mChatAdapter.addItem(it)
                    it.chat?.let{Log.d("chatting", it)}
                    if(it.chat == null){
                        Log.d("chatting","null")
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
        })


//        databaseReference.child(objectid).get()




    }

}