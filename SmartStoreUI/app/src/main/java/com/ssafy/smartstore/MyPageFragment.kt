package com.ssafy.smartstore

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
import com.ssafy.smartstore.databases.History
import com.ssafy.smartstore.databinding.FragmentMyPageBinding
import com.ssafy.smartstore.dto.OrderMap
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment() {

    private lateinit var ctx: Context
    private lateinit var myPageAdapter: HistoryAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return FragmentMyPageBinding.inflate(inflater, container, false).apply {
            val prefs = activity?.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)

            tvNickname.text = prefs!!.getString("name", "정보 없음")

            // 로그 아웃 버튼 클릭 시, 로그인 화면으로 & Shared Preference
            imgLogout.setOnClickListener {
                val editor = prefs!!.edit()
                editor.remove("id")
                editor.remove("pwd")
                editor.remove("name")
                editor.remove("stamps")
                editor.commit()

                activity?.finish()
            }

            myPageAdapter = HistoryAdapter(ctx, "MyPage")

            CoroutineScope(Dispatchers.Main).launch {
                getItems(prefs!!.getString("id", "").toString())
            }
            // 주문 내역 표기
            listView.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            listView.adapter = myPageAdapter

            val itemClickListener = object : HistoryAdapter.OnItemClickListener{
                override fun onItemClick(view: View, position: Int) {
                    Log.d("position", "$position")
                    val intent = Intent(getActivity(), OrderDetailActivity::class.java)
                    intent.putExtra("data", myPageAdapter.objects[position].o_id)
                    intent.putExtra("date", myPageAdapter.objects[position].order_time)
                    startActivity(intent)
                }
            }
            myPageAdapter.onItemClickListener = itemClickListener

        }.root
    }
    /*
    // F07: 회원관리/주문관리 - 회원 정보 조회 - Id 기반으로 회원의 상세 정보를 조회할 수 있다. 이때 회원의 정보와 함께 최근 주문 내역 및 회원 등급 정보를 반환할 수 있다.
    * */
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

                myPageAdapter.objects = objects

                for(i in objects)
                    Log.d("TAG", "onResponse: ${i.name}, ${i.o_id}")

                this.launch(Dispatchers.Main){
                    myPageAdapter.notifyDataSetChanged()
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

//    suspend fun getId(user_id: String): MutableList<Int>{
//        return repo.getId(user_id)
//    }
}