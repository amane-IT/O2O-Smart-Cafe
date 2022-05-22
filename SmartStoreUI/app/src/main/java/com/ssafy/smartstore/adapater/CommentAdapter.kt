package com.ssafy.smartstore.adapater

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.IntentApplication
import com.ssafy.smartstore.OrderActivity
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dialog.UpdateDialog
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.service.CommentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommentAdapter(val context: Context): RecyclerView.Adapter<CommentAdapter.ItemViewHolder>() {
    var objects: List<Comment> = emptyList()
    var user_id: String = ""
    val commentService = IntentApplication.retrofit.create(CommentService::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.comment_list, parent, false)

        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.apply {
            comment.text = objects[position].comment

            if(user_id != objects[position].userId){
                btnDelete.visibility = View.GONE
                btnUpdate.visibility = View.GONE
            }
            else {
                btnDelete.visibility = View.VISIBLE
                btnUpdate.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return objects.size
    }

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
    }

    lateinit var onItemClickListener: OnItemClickListener

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val comment = itemView.findViewById(R.id.tv_comment) as TextView
        val btnUpdate = itemView.findViewById(R.id.btn_update) as ImageButton
        val btnDelete = itemView.findViewById(R.id.btn_delete) as ImageButton
        private val instance = OrderActivity.getInstance()

        init {
            btnUpdate.setOnClickListener {
                val dialog = UpdateDialog(context, objects[position])
                dialog.start()
            }
            btnDelete.setOnClickListener {
                val dialog = AlertDialog.Builder(context)
                    .setTitle("코멘트를 삭제하시겠습니까?")
                    .setMessage("'${objects[position].comment}'를 삭제하시겠습니까?")
                    .setPositiveButton("네", DialogInterface.OnClickListener { dialog, id ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = commentService.delete(objects[position].id).execute()
                            if(response.code() == 200){
                                var res = response.body()
                                if(res == false){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Log.d("INFO", ": 삭제 실패")
                                        Toast.makeText(context, "삭제 실패!", Toast.LENGTH_SHORT).show()
                                    }
                                    null
                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, "성공!", Toast.LENGTH_SHORT).show()
                                        Log.d("INFO", ": 삭제 성공")
                                        instance!!.updateList()
                                        instance!!.updateRating(objects[0].productId)
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d("INFO", ":통신 실패")
                                    Toast.makeText(context, "통신 실패!", Toast.LENGTH_SHORT).show()
                                }
                                null
                            }
                        }
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener{ dialog, which ->
                    })
                dialog.show()
            }
        }
    }
}