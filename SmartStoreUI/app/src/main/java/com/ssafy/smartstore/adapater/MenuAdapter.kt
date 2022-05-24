package com.ssafy.smartstore.adapater

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dto.Product

class MenuAdapter(private val context: Context): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    var listData: List<Product> = emptyList()
    var topList: List<Int> = emptyList()
    var best = 0

    inner class MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val item = itemView.findViewById<ImageButton>(R.id.btn_menu_item)
        val item_name= itemView.findViewById<TextView>(R.id.tv_name)
        val top = itemView.findViewById<ImageView>(R.id.is_best)
        val favorite = itemView.findViewById<ImageView>(R.id.is_favorite)

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

            if(topList.contains(dto.id)){
                if(topList.indexOf(dto.id) == 0){
                    top.setImageDrawable(context.resources.getDrawable(R.drawable.ic_gold))
                } else if(topList.indexOf(dto.id) == 1){
                    top.setImageDrawable(context.resources.getDrawable(R.drawable.ic_silver))
                } else{
                    top.setImageDrawable(context.resources.getDrawable(R.drawable.ic_bronze))
                }
            }

            if(best != 0){
                if(best == dto.id){
                    Log.d("TAG", "onBindViewHolder: $best , ${dto.id}")
                    favorite.setBackgroundResource(R.drawable.round_star_24)
                }
            }
        }
        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        return listData.size
    }
}