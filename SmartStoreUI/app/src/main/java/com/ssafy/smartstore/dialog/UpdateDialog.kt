package com.ssafy.smartstore.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.*
import com.ssafy.smartstore.IntentApplication
import com.ssafy.smartstore.OrderActivity
import com.ssafy.smartstore.R
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.service.CommentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch

class UpdateDialog(context: Context, comment: Comment) {
    private val dialog = Dialog(context)
    private lateinit var btnOk: Button
    private lateinit var etvContent: EditText
    private lateinit var ratingScore: RatingBar
    private lateinit var tvScore: TextView
    private var comment = comment

    private val commentService = IntentApplication.retrofit.create(CommentService::class.java)

    fun start() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_update)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        etvContent = dialog.findViewById(R.id.comment_content)
        tvScore = dialog.findViewById(R.id.comment_score)
        ratingScore = dialog.findViewById(R.id.rating_score)
        btnOk = dialog.findViewById(R.id.btn_ok)

        etvContent.setText(comment.comment)
        Log.d("TAG", "start: ${comment.comment}")
        tvScore.text = "${comment.rating}점"
        ratingScore.rating = comment.rating.toFloat() / 2

        ratingScore.onRatingBarChangeListener = Listener()

        btnOk.setOnClickListener {
            comment.rating = ratingScore.rating.toDouble() * 2
            comment.comment = etvContent.text.toString()
            val instance = OrderActivity.getInstance()

            CoroutineScope(Dispatchers.IO).launch {
                val response = commentService.update(comment).execute()
                val result = if(response.code() == 200){
                    var res = response.body()
                    if(res == false){
                        Log.d("TAG", "start: $comment")
                        //Toast.makeText(this@UpdateDialog, "update 실패!", Toast.LENGTH_SHORT).show()
                        false
                    } else{
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("TAG", "start: 성공")
                            //Toast.makeText(this@UpdateDialog, "update 성공!", Toast.LENGTH_SHORT).show()
                            instance!!.updateList()
                            instance!!.updateRating(comment.productId)
                            dialog.dismiss()
                        }
                        true
                    }
                } else{
                    Log.d("TAG", "start: 성공")
                }
            }
        }

        dialog.show()
    }

    // 별점이 1점 이하로 내려가지 않도록 한다
    inner class Listener : RatingBar.OnRatingBarChangeListener{
        override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
            if (ratingScore.rating < 0.5f){
                ratingScore.rating = 0.5f
            }else{
                ratingScore.rating = rating
            }
            tvScore.text = "${ratingScore.rating * 2}점"
        }
    }
}