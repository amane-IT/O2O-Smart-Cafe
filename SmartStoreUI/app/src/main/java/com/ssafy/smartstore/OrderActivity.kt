package com.ssafy.smartstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.smartstore.adapater.CommentAdapter
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.databinding.ActivityOrderBinding
import com.ssafy.smartstore.dialog.RatingDialog
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.dto.ProductDetail
import com.ssafy.smartstore.service.CommentService
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.round

// F06: 상품 추가 화면, 장바구니에 등록.

private const val TAG = "OrderActivity_싸피"
class OrderActivity : AppCompatActivity() {

    companion object{
        private var instance: OrderActivity? = null
        private var id = 0
        private var data: Product? = null
        private var adapter: CommentAdapter? = null
        private lateinit var binding: ActivityOrderBinding

        fun getInstance(): OrderActivity?{
            if(instance == null)
                instance = OrderActivity()

            return instance
        }
    }

    val commentService = IntentApplication.retrofit.create(CommentService::class.java)
    val productService = IntentApplication.retrofit.create(ProductService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = intent.getSerializableExtra("data") as Product
        id = data!!.id

        var result: ProductDetail? = null
        CoroutineScope(Dispatchers.IO).launch {
            result = setData()//

            // 별점
            CoroutineScope(Dispatchers.Main).launch {
                binding.tvRating.text = "${round(result!!.avg * 10) / 10}점"
                binding.ratingMenu.rating = (round(result!!.avg * 10) / 10).toFloat() / 2
            }
        }

        val resId = resources.getIdentifier("${data!!.img.substring(0, data!!.img.length - 4)}", "drawable", packageName)
        binding.ivImg.setImageResource(resId)
        binding.tvName.text = data!!.name
        binding.tvPrice.text = "${data!!.price}원"
        binding.tvCount.text = "1"

        // comment 리스트
        binding.commentList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = CommentAdapter(this)
        adapter!!.user_id = getSharedPreferences("prefs", MODE_PRIVATE).getString("id", "").toString()

        val itemClickListener = object : CommentAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {

            }
        }
        adapter!!.onItemClickListener = itemClickListener

        CoroutineScope(Dispatchers.IO).launch {
            updateList()
        }

        // 등록
        binding.btnComment.setOnClickListener {
            val text = binding.edComment.text.toString()
            val comment = Comment(id, adapter!!.user_id, 0.0, text)
            binding.edComment.setText("")

            val dialog = RatingDialog(this, comment)
            dialog.start()
        }

    }

    fun setData() : ProductDetail{
        val response = productService.getProduct(id).execute()
        val result = if(response.code() == 200){
            var res = response.body()
            if(res == null){
                Toast.makeText(this, "가져오기 실패", Toast.LENGTH_SHORT).show()
                null
            } else{
                Log.d(TAG, "setData: ${res}")
                data = Product(id, res[0].name, res[0].type, res[0].price, res[0].img)
                res[0]
            }
        } else{
            Toast.makeText(this, "통신 실패", Toast.LENGTH_SHORT).show()
            null
        }
        return result!!
    }

    fun updateList(){
        CoroutineScope(Dispatchers.IO).launch {
            val response = commentService.getComments(data!!.id).execute()
            val result = if(response.code() == 200){
                var res = response.body()
                Log.d(TAG, "onCreate: ${res}")
                if(res == null)
                    emptyList<Comment>()
                else
                    res!!
            } else
                emptyList()
            if(result != emptyList<Comment>()) {
                adapter!!.objects = result
                CoroutineScope(Dispatchers.Main).launch {
                    binding.commentList.adapter = adapter
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    fun updateRating(id: Int){
        CoroutineScope(Dispatchers.IO).launch {
            val response = productService.getAvgRating(id).execute()
            if(response.code() == 200){
                var res = response.body()
                if(res != null){
                    CoroutineScope(Dispatchers.Main).launch {
                        binding.tvRating.text = "${round(res!! * 10) / 10}점"
                        binding.ratingMenu.rating = (round(res * 10) / 10).toFloat() / 2
                    }
                }
            } else{

            }
        }
    }

    fun add(view: View) {
        var cnt = binding.tvCount.text.toString().toInt() + 1
        binding.tvPrice.text = "${data!!.price * cnt}원"
        binding.tvCount.text = cnt.toString()
    }

    fun minus(view: View) {
        var cnt = binding.tvCount.text.toString().toInt() - 1
        binding.tvPrice.text = "${data!!.price * cnt}원"
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