package com.ssafy.smartstore.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import com.ssafy.smartstore.JoinActivity
import com.ssafy.smartstore.R
import java.util.*

class DateDialog(context: Context) {
    private val dialog = Dialog(context)
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var monthPicker: NumberPicker
    private lateinit var dayPicker: NumberPicker

    fun start(){

        val cal = Calendar.getInstance()

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        btnCancel = dialog.findViewById(R.id.btn_cancel)
        btnSave = dialog.findViewById(R.id.btn_confirm)
        monthPicker = dialog.findViewById(R.id.picker_month)
        dayPicker = dialog.findViewById(R.id.picker_day)

        Log.d("TAG", "start: ${cal.get(Calendar.MONTH) + 1},  ${cal.get(Calendar.DAY_OF_MONTH)}")
        val tmp = (cal.get(Calendar.MONTH) + 1)
        Log.d("TAG", "start: ${tmp}")
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = tmp

        dayPicker.minValue = 1
        dayPicker.maxValue =
            when(monthPicker.value){
                1, 3, 5, 7, 8, 10, 12 -> 31
                2 -> 29
                else -> 30
            }
        monthPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            dayPicker.maxValue =
                when(newVal){
                    1, 3, 5, 7, 8, 10, 12 -> 31
                    2 -> 29
                    else -> 30
                }
        }
        dayPicker.value = cal.get(Calendar.DAY_OF_MONTH)

        setNumberPickerTextColor(monthPicker, R.attr.txtColor)
        setNumberPickerTextColor(dayPicker, R.attr.txtColor)


        btnSave.setOnClickListener {
            val instance = JoinActivity.getInstanceJoin()
            val month = monthPicker.value
            val day = dayPicker.value

            instance.setBirthdayText("${month}월 ${day}일")
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val count = numberPicker.childCount
            for (i in 0..count) {
                val child = numberPicker.getChildAt(i)
                if (child is EditText) {
                    try {
                        child.setTextColor(color)
                        numberPicker.invalidate()
                        var selectorWheelPaintField = numberPicker.javaClass.getDeclaredField("mSelectorWheelPaint")
                        var accessible = selectorWheelPaintField.isAccessible
                        selectorWheelPaintField.isAccessible = true
                        (selectorWheelPaintField.get(numberPicker) as Paint).color = color
                        selectorWheelPaintField.isAccessible = accessible
                        numberPicker.invalidate()
                        var selectionDividerField = numberPicker.javaClass.getDeclaredField("mSelectionDivider")
                        accessible = selectionDividerField.isAccessible
                        selectionDividerField.isAccessible = true
                        selectionDividerField.set(numberPicker, null)
                        selectionDividerField.isAccessible = accessible
                        numberPicker.invalidate()
                    } catch (exception: Exception) {
                        Log.d("test", "exception $exception")
                    }
                }
            }
        } else {
            numberPicker.textColor = color
        }
    }

}