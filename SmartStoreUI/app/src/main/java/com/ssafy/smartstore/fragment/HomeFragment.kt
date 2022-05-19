package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.smartstore.IntentApplication
import com.ssafy.smartstore.OrderDetailActivity
import com.ssafy.smartstore.R
import com.ssafy.smartstore.adapater.HistoryAdapter
import com.ssafy.smartstore.adapater.NoticeAdapter
import com.ssafy.smartstore.databinding.FragmentHomeBinding
import com.ssafy.smartstore.dto.OrderMap
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var ctx: Context
    private val noticeList : MutableList<String> = mutableListOf()
    private lateinit var homeAdapter: HistoryAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // 알림판 RecyclerView
        noticeList.add("오늘 적립한 스탬프의 개수는 5개 입니다.")
        noticeList.add("주문하신 메뉴가 나왔습니다.")

        return FragmentHomeBinding.inflate(inflater, container, false).apply {

            listNotice.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
            val adapter = NoticeAdapter(ctx, R.layout.item_notice, noticeList)
            listNotice.adapter = adapter

            homeAdapter = HistoryAdapter(ctx, "Home")
            val prefs = activity?.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)

            CoroutineScope(Dispatchers.Main).launch {
                getItems(prefs!!.getString("id", "").toString())
            }

            tvNickname.text = prefs!!.getString("name", "").toString() + "님"

            Log.d("TAG", "onCreateView: $ctx")
            listOrder.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            listOrder.adapter = homeAdapter

            val itemClickListener = object : HistoryAdapter.OnItemClickListener{
                override fun onItemClick(view: View, position: Int) {
                    Log.d("position", "$position")
                    val intent = Intent(getActivity(), OrderDetailActivity::class.java)
                    intent.putExtra("data", homeAdapter.objects[position].o_id)
                    intent.putExtra("date", homeAdapter.objects[position].order_time)
                    startActivity(intent)
                }
            }
            homeAdapter.onItemClickListener = itemClickListener

        }.root
    }

    // 최근 1개월간의 주문 목록을 가져옴
    suspend fun getItems(user_id: String) {
        val result: List<OrderMap>? = withContext(Dispatchers.IO){
            val orderService = IntentApplication.retrofit.create(OrderService::class.java)
            val response = orderService.getLastMonthOrders(user_id).execute()
            val list = if(response.code() == 200){
                var res = response.body() ?: emptyList()
                val list = mutableListOf<Int>()
                val objects = mutableListOf<OrderMap>()

                for(i in res){
                    if(list.contains(i.o_id)) {
                        for(j in objects){
                            if(j.o_id == i.o_id){
                                j.others++
                                break
                            }
                        }
                        continue
                    }
                    objects.add(i)
                    list.add(i.o_id)
                }

                homeAdapter.objects = objects

                for(i in objects)
                    Log.d("TAG", "onResponse: ${i.name}, ${i.o_id}")

                this.launch(Dispatchers.Main){
                    homeAdapter.notifyDataSetChanged()
                }
                res
            }
            else {
                Log.d("ERROR", "getItems: ERROR: ${response.code()}")
                emptyList()
            }
            list
        }
    }

    // order detail id들을 반환하는 getId 함수 삭제함.
}