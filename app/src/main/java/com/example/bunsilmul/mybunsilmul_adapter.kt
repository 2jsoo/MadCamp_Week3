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



class mybunsilmul_adapter(val context: Context): RecyclerView.Adapter<mybunsilmul_adapter.ViewHolder>(){
    //    private var items: List<ContactModel> = emptyList()
    private var mybunsilmuls = arrayOf<bunsilmul>()

//    fun addItem(mybunsilmul: bunsilmul){
//        mybunsilmuls.add(mybunsilmul)
//        notifyDataSetChanged()
//    }

    fun bindItem(mybunsilmuls: Array<bunsilmul>){
        this.mybunsilmuls = mybunsilmuls
        notifyDataSetChanged()
    }

//    fun deleteItem(mybunsilmul: bunsilmul){
//        mybunsilmuls.remove(mybunsilmul)
////        notifyDataSetChanged()
//    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
//        var mychattext: TextView = itemView.findViewById(R.id.mychat)
//        var click: View = itemView.findViewById(R.id.mychat_item_click)

        var mycategory:TextView = itemView.findViewById(R.id.mybunsilmul_category)
        var myinformation:TextView = itemView.findViewById(R.id.mybunsilmul_infromation)
        var click:View = itemView.findViewById(R.id.mybunsilmul_item_click)


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mybunsilmul_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val chat = holder.bindItem(chats[position])
        val mybunsilmul = mybunsilmuls[position]
        holder.setIsRecyclable(false)

        holder.mycategory.text = mybunsilmul.category
        holder.myinformation.text = mybunsilmul.information

//        if(mychat.mine){ // 내가 올린 분실물
//            holder.mychattext.text = "내가 올린 분실물"
//        } else { //내가 찾고 싶은
//            holder.mychattext.text = "내가 찾고 싶은 분실물"
//        }


        holder.click.setOnClickListener {
            val intent = Intent(context, Activity_mybunsilmul_bunsilmul::class.java)


            mybunsilmul._id?.let{intent.putExtra("id", it)}
            mybunsilmul.category?.let{intent.putExtra("category", it)}
            mybunsilmul.information?.let{intent.putExtra("information", it)}
            mybunsilmul.uid?.let{intent.putExtra("uid",it)}

            startActivity(context,intent,null)

        }




    }

    override fun getItemCount(): Int {
        return mybunsilmuls.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }



}