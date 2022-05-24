package com.ssafy.smartstore.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.ssafy.smartstore.R
import android.view.Window
import android.widget.Button
import android.widget.TextView

private const val TAG = "MapInfoDialog_싸피"
class MapInfoDialog(context: Context) {
    private val ctx = context
    private val dialog = Dialog(ctx)

    private lateinit var tvCafeName: TextView

    private lateinit var btnSearchDir: Button
    private lateinit var btnCall: Button

    fun start(cafeName: String) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_map_info)

        tvCafeName = dialog.findViewById(R.id.tv_cafe_name)
        btnSearchDir = dialog.findViewById(R.id.btn_search_direction)
        btnCall = dialog.findViewById(R.id.btn_call)

        tvCafeName.text = cafeName

        val phonenumber = "010-0000-0000"

        btnSearchDir.setOnClickListener {
            Log.d(TAG, "start: 길찾기 버튼")
        }
        btnCall.setOnClickListener {
            Log.d(TAG, "start: 전화걸기 버튼")
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + phonenumber)))
        }
        dialog.show()
    }
}