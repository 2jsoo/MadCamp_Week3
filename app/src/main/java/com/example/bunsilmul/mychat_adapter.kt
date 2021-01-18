package com.example.bunsilmul

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

public class myChat(
        var objectid: String,
        var mine: Boolean
)


class mychat_adapter(val context: Context): RecyclerView.Adapter<mychat_adapter.ViewHolder>(){
    //    private var items: List<ContactModel> = emptyList()
    private var mychats = mutableListOf<myChat>()

    fun addItem(objectid: myChat){
        mychats.add(objectid)
        notifyDataSetChanged()
    }

    fun bindItem(mychats: MutableList<myChat>){
        this.mychats = mychats
        notifyDataSetChanged()
    }

    fun deleteItem(mychat: myChat){
        mychats.remove(mychat)
//        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var mychattext: TextView = itemView.findViewById(R.id.mychat)
        var click: View = itemView.findViewById(R.id.mychat_item_click)


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mychat_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val chat = holder.bindItem(chats[position])
        val mychat = mychats[position]
        holder.setIsRecyclable(false)

        if(mychat.mine){ // 내가 올린 분실물
            holder.mychattext.text = "내가 올린 분실물"
        } else { //내가 찾고 싶은
            holder.mychattext.text = "내가 찾고 싶은 분실물"
        }


        holder.click.setOnClickListener {
            val intent = Intent(context, Activity_mychat_bunsilmul::class.java)
            mychat.objectid?.let{intent.putExtra("id", it)}
            startActivity(context,intent,null)
        }




    }

    override fun getItemCount(): Int {
        return mychats.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }



}