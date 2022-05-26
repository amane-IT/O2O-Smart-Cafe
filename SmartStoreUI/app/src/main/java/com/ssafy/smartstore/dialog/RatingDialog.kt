package com.ssafy.smartstore.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import com.ssafy.smartstore.IntentApplication
import com.ssafy.smartstore.OrderActivity
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.service.CommentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RatingDialog(context: Context, comment: Comment) {
    private val dialog = Dialog(context)
    private val commentService = IntentApplication.retrofit.create(CommentService::class.java)

    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private lateinit var ratingScore: RatingBar
    private var comment = comment
    private val context = context

    fun start() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_rating)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        ratingScore = dialog.findViewById(R.id.rating_score)
        btnOk = dialog.findViewById(R.id.btn_save)
        btnOk.setOnClickListener {

            val rate = ratingScore.rating.toDouble()
            comment.rating = rate * 2
            CoroutineScope(Dispatchers.IO).launch {
                val response = commentService.insert(comment).execute()
                if(response.code() == 200){
                    var res = response.body()
                    if(res != null){
                        if(res){
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                                val instance = OrderActivity.getInstance()
                                instance!!.updateList()
                                dialog.dismiss()
                            }
                        } else{
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "코멘트 저장 실패헸습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else{
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "통신 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnCancel = dialog.findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}