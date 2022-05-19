package com.ssafy.smartstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.smartstore.adapater.ItemAdapter
import com.ssafy.smartstore.fragment.Shopping
import com.ssafy.smartstore.databinding.ActivityOrderDetailBinding
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

// F08: 주문관리 - 주문 상세 조회 - 주문 번호에 기반하여 주문을 조회할 수 있다. 이때 주문 상세 항목들 어떤 상품이 몇개 주문 되었는지에 대한 정보도 포함한다.

private const val TAG = "OrderDetailActivity_싸피"
class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding

    private var orderList = mutableListOf<Shopping>()
    val orderService = IntentApplication.retrofit.create(OrderService::class.java)
    val adapter = ItemAdapter(this@OrderDetailActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.listView.adapter = adapter
        binding.listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val text = intent.getStringExtra("date")
        val idx = text!!.indexOf("T")
        binding.tvOrderDate.text = text!!.substring(0, idx)

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "onCreate: ${intent.getIntExtra("data", -1)}")
            val order_id = intent.getIntExtra("data", -1)
            getDetail(order_id)
        }

        val itemClickListener = object : ItemAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {

            }
        }
        adapter.onItemClickListener = itemClickListener


    }

    suspend fun getDetail(order_id: Int) {
        withContext(Dispatchers.IO){
            val response = orderService.getOrder(order_id).execute()
            if(response.code() == 200){
                var res = response.body() ?: mutableListOf<Map<String, Object>>()
                var completed = "N"

                for(i in res){
                    val shop = Shopping(i.get("img").toString(), i.get("name").toString(), i.get("unitprice").toString().toDouble().toInt(), i.get("quantity").toString().toDouble().toInt())
                    if(!orderList.contains(shop))
                        orderList.add(shop)
                    completed = i.get("completed").toString()
                }

                var price = 0
                for(i in orderList){
                    price += i.price * i.quantity
                }

                val dec = DecimalFormat("#,###원")

                adapter.objects = orderList
                this.launch(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    binding.tvOrderStatus.text = if(completed.equals("N")) "진행 중.." else "주문 완료"
                    binding.tvOrderPrice.text = dec.format(price)
                }
            }
        }
    }
}