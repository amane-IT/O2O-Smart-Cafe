package com.ssafy.smartstore.adapater

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dto.Product

class MenuAdapter(private val context: Context): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    var listData: List<Product> = emptyList()

    inner class MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val item = itemView.findViewById<ImageButton>(R.id.btn_menu_item)
        val item_name= itemView.findViewById<TextView>(R.id.tv_name)

        fun bindOnItemClickListener(onItemClickListener: OnItemClickListener) {
            item.setOnClickListener {
                //adapterPosition: ViewHolder에서 제공하는 idx를 알려주는 함수
                Log.d("TAG", "bindOnItemClickListener: $adapterPosition")
                onItemClickListener.onItemClick(it, adapterPosition)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    lateinit var onItemClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_menu, parent, false)

        return MenuViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val dto = listData[position]
        Log.d("dto", dto.img.substring(0, dto.img.length - 4))
        holder.apply {
            val resId = context.resources.getIdentifier("${dto.img.substring(0, dto.img.length - 4)}", "drawable", context.packageName)
            item.setImageResource(resId)
            item_name.text = dto.name
        }
        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}