package com.ssafy.smartstore

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.dto.OrderMap
import java.text.DecimalFormat

private val TAG = "Adapter_싸피"
class HistoryAdapter(val context: Context, val name: String): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    var objects: List<OrderMap> = emptyList()

    inner class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var layout = itemView.findViewById(R.id.item_order) as LinearLayout
        var img = itemView.findViewById(R.id.img_item) as ImageView
        var content = itemView.findViewById(R.id.tv_contents) as TextView
        var price = itemView.findViewById(R.id.tv_price) as TextView
        var date = itemView.findViewById(R.id.tv_date) as TextView
        var cart = itemView.findViewById(R.id.btn_cart) as Button
        var pickup = itemView.findViewById(R.id.btn_pickup) as Button

        fun bindOnItemClickListener(onItemClickListener: OnItemClickListener) {
            layout.setOnClickListener {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_order, parent, false)

        return HistoryViewHolder(itemView)
    }

    // 사진, 주문 내역, 가격, 날짜
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.apply {
            val resId = context.resources.getIdentifier("${objects[position].img.substring(0, objects[position].img.length - 4)}", "drawable", context.packageName)

            val dec = DecimalFormat("#,###원")

            img.setImageResource(resId)
            if(objects[position].others != 0)
                content.text = objects[position].name + " 외 ${objects[position].others}개"
            else
                content.text = objects[position].name

            price.text = dec.format(objects[position].price)

            val idx = objects[position].order_time.indexOf("T")
            date.text = objects[position].order_time.substring(0, idx)

            if(name.equals("Home")){
                cart.visibility = View.VISIBLE
                pickup.visibility = View.GONE
            }
            else {
                pickup.visibility = View.VISIBLE
                if(objects[position].completed.equals("N"))
                    pickup.text = "진행 중.."

                cart.visibility = View.GONE
            }
        }
        holder.bindOnItemClickListener(onItemClickListener)
    }

    override fun getItemCount(): Int {
        // objects 크기
        return objects.size
    }
}

