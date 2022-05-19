package com.ssafy.smartstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.databinding.ActivityOrderBinding

// F06: 상품 추가 화면, 장바구니에 등록.

private const val TAG = "OrderActivity_싸피"
class OrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderBinding
    private lateinit var data: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = intent.getSerializableExtra("data") as Product
        val resId = resources.getIdentifier("${data.img.substring(0, data.img.length - 4)}", "drawable", packageName)
        binding.ivImg.setImageResource(resId)
        binding.tvName.text = data.name
        binding.tvPrice.text = "${data.price}원"
        binding.tvCount.text = "1"

    }

    fun add(view: View) {
        var cnt = binding.tvCount.text.toString().toInt() + 1
        binding.tvPrice.text = "${data.price * cnt}원"
        binding.tvCount.text = cnt.toString()
    }

    fun minus(view: View) {
        var cnt = binding.tvCount.text.toString().toInt() - 1
        binding.tvPrice.text = "${data.price * cnt}원"
        binding.tvCount.text = cnt.toString()
    }

    fun goList(view: View) {
        val intent = Intent(this, ShoppingListActivity::class.java)
        intent.putExtra("data", data)
        intent.putExtra("qty", binding.tvCount.text.toString().toInt())
        intent.putExtra("from", "order")
        startActivity(intent)
        finish()
    }

}