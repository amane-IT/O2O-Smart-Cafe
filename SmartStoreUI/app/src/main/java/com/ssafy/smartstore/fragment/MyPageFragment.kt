package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
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
import com.ssafy.smartstore.databinding.FragmentMyPageBinding
import com.ssafy.smartstore.dto.OrderMap
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.*

class MyPageFragment : Fragment() {

    private lateinit var ctx: Context
    private lateinit var myPageAdapter: HistoryAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return FragmentMyPageBinding.inflate(inflater, container, false).apply {
            val prefs = activity?.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)

            tvNickname.text = prefs!!.getString("name", "정보 없음")

            // 스탬프 관련 UI 변경
            var level = IntentApplication.grade
            val resId = ctx.resources.getIdentifier("${level.img.substring(0, level.img.length - 4)}", "drawable", ctx.packageName)
            stampImg.setImageResource(resId)

            tvStamp.text = "${level.title} ${level.step}단계"

            if(level.title.equals("씨앗"))
                stampProgress.max = 10
            else if (level.title.equals("꽃"))
                stampProgress.max = 15
            else if (level.title.equals("열매"))
                stampProgress.max = 20
            else if (level.title.equals("커피콩"))
                stampProgress.max = 25
            else {
                stampProgress.max = 100
                stampProgress.progress = 100
                statusProgress.text = "☆ 나무입니다 ☆"
                tvRemain.text = "다 모았습니다."
            }

            if(!level.title.equals("나무")){
                stampProgress.progress = stampProgress.max - level.to
                statusProgress.text = "${stampProgress.progress}/${stampProgress.max}"
                tvRemain.text = "다음 레벨까지 앞으로 ${level.to}잔 남았습니다."
            }

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

            val theme = ctx.getSharedPreferences("theme", AppCompatActivity.MODE_PRIVATE)
            val themeId = theme.getInt("id", 1)

            if(themeId == 1){
                radio1.isChecked = true
            } else if(themeId == 2){
                radio2.isChecked = true
            } else {
                radio3.isChecked = true
            }

            // 라디오버튼
            themeGroup.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.radio1 -> {
                        if(themeId != 1){
                            val editor = theme.edit()
                            editor.putInt("id", 1)
                            editor.commit()
                            activity?.finish()
                            startActivity(activity?.intent)
                        }
                    }
                    R.id.radio2 -> {
                        if(themeId != 2){
                            val editor = theme.edit()
                            editor.putInt("id", 2)
                            editor.commit()
                            activity?.finish()
                            startActivity(activity?.intent)
                        }
                    }
                    R.id.radio3 -> {
                        if(themeId != 3){
                            val editor = theme.edit()
                            editor.putInt("id", 3)
                            editor.commit()
                            activity?.finish()
                            startActivity(activity?.intent)
                        }
                    }
                }
            }
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
}