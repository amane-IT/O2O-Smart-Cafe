package com.ssafy.smartstore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.databases.Shopping

class ItemAdapter(val context: Context ): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    var objects: List<Shopping> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_detail, parent, false)

        return ItemViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.apply {
            val resId = context.resources.getIdentifier("${objects[position].img.substring(0, objects[position].img.length - 4)}", "drawable", context.packageName)
            img.setImageResource(resId)
            name.text = objects[position].name
            price.text = objects[position].price.toString() + "원"
            qty.text = objects[position].quantity.toString() + "잔"
            totalPrice.text = (objects[position].price * objects[position].quantity).toString() + "원"

            if(context::class.java.simpleName.equals("OrderDetailActivity")){
                btnDelete.visibility = View.GONE
            }
            else if(context::class.java.simpleName.equals("ShoppingListActivity")){
                btnDelete.visibility = View.VISIBLE
            }
        }
        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        return objects.size
    }

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    lateinit var onItemClickListener: OnItemClickListener

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById(R.id.img_detail_img) as ImageView
        val name = itemView.findViewById(R.id.tv_name) as TextView
        val price = itemView.findViewById(R.id.tv_price) as TextView
        val qty = itemView.findViewById(R.id.tv_qty) as TextView
        val totalPrice = itemView.findViewById(R.id.tv_total_price) as TextView
        val btnDelete = itemView.findViewById(R.id.btn_delete) as Button

        fun bindOnItemClickListener(onItemClickListener: OnItemClickListener) {
            btnDelete.setOnClickListener {
                //adapterPosition: ViewHolder에서 제공하는 idx를 알려주는 함수
                Log.d("TAG", "bindOnItemClickListener: $adapterPosition")
                onItemClickListener.onItemClick(it, adapterPosition)
            }
        }
    }
}