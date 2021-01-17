package com.example.bunsilmul

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

public class Chat(
    var uid: String? = "",
    var chat: String? = ""
)

class Chat_Adapter(): RecyclerView.Adapter<Chat_Adapter.ViewHolder>(){
    //    private var items: List<ContactModel> = emptyList()
    private var chats = mutableListOf<Chat>()

    fun addItem(chat: Chat){
        chats.add(chat)
        chat.chat?.let { Log.d("CHATTING ADD", it) }
        if(chat.chat == null){
            Log.d("CHATTING ADD","NULL")
        }
        notifyDataSetChanged()
    }

    fun bindItem(chats: MutableList<Chat>){
        this.chats = chats
        notifyDataSetChanged()
    }

    fun deleteItem(chat: Chat){
        chats.remove(chat)
//        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mytext: TextView = itemView.findViewById(R.id.my_text)
        var yourtext: TextView = itemView.findViewById(R.id.your_text)


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val chat = holder.bindItem(chats[position])
        val chat = chats[position]
        holder.setIsRecyclable(false)

        if(chat.uid == FirebaseAuth.getInstance().currentUser?.uid){
            holder.mytext.text = chat.chat
        } else {
            holder.yourtext.text = chat.chat
        }

    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }



}