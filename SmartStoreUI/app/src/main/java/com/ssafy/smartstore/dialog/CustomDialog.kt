package com.ssafy.smartstore.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.Button
import com.ssafy.smartstore.R

// 사용자가 매장 안에 있을 경우 아래 팝업을 띄운다.
class CustomDialog(context: Context) {
    private val dialog = Dialog(context)
    private lateinit var btnOk: Button

    fun start() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.setCancelable(false)

        btnOk = dialog.findViewById(R.id.btn_ok)
        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}