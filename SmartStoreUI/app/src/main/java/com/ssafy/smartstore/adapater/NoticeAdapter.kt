package com.ssafy.smartstore.adapater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.R

class NoticeAdapter(val context: Context, val resource: Int, val objects: MutableList<String>): RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val notice = itemView.findViewById<TextView>(R.id.tv_notice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(resource, parent, false)

        return NoticeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        holder.notice.text = objects[position]
    }

    override fun getItemCount(): Int {
        return objects.size
    }
}