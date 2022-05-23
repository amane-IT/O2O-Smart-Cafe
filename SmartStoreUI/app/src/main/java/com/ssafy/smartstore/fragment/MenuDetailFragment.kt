package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.smartstore.IntentApplication
import com.ssafy.smartstore.MapActivity
import com.ssafy.smartstore.OrderActivity
import com.ssafy.smartstore.ShoppingListActivity
import com.ssafy.smartstore.adapater.MenuAdapter
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.databinding.FragmentMenuDetailBinding
import com.ssafy.smartstore.dto.Favorite
import com.ssafy.smartstore.service.FavoriteService
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.ProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class MenuDetailFragment : Fragment() {

    private lateinit var ctx: Context

    private lateinit var menuAdapter: MenuAdapter
    private var fList = mutableListOf<Favorite>()
    private var userId: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
        initAdapter()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return FragmentMenuDetailBinding.inflate(inflater, container, false).apply {
            listMenu.apply {
                layoutManager = GridLayoutManager(ctx, 3)
                adapter = menuAdapter
            }
            
            val itemClickListener = object : MenuAdapter.OnItemClickListener{
                override fun onItemClick(view: View, position: Int) {
                    val intent = Intent(getActivity(), OrderActivity::class.java)
                    intent.putExtra("data", menuAdapter.listData[position])
                    for(i in fList){
                        if(i.productId == menuAdapter.listData[position].id){
                            intent.putExtra("favorite", i)
                            intent.putExtra("flag", true)
                            break
                        }
                    }
                    startActivity(intent)
                }
            }
            menuAdapter.onItemClickListener = itemClickListener

            btnMap.setOnClickListener {
                startActivity(Intent(ctx, MapActivity::class.java))
            }

            btnCart.setOnClickListener{
                val intent = Intent(ctx, ShoppingListActivity::class.java)
                intent.putExtra("from", "main")
                startActivity(intent)
            }
        }.root
    }

    private fun initAdapter(){
        menuAdapter = MenuAdapter(ctx)
        getData()
        getFavorite()
        getTop3()
        getUserMenu()
        menuAdapter.notifyDataSetChanged()

    }

    override fun onResume() {
        super.onResume()
        menuAdapter.notifyDataSetChanged()

    }

    private fun getData() {
        val productService = IntentApplication.retrofit.create(ProductService::class.java)
        productService.getAllProduct().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                val res = response.body()
                if (response.code() == 200) {
                    menuAdapter.listData = emptyList()
                    if (res != null) {
                        menuAdapter.listData = res
                    } else {
                        Toast.makeText(activity, "상품 정보를 가져올 수 없음!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.d("TAG", "onFailure: 통신 오류!")
            }
        })
    }

    private fun getFavorite(){
        val fService = IntentApplication.retrofit.create(FavoriteService::class.java)
        userId = activity?.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)?.getString("id", "")
        CoroutineScope(Dispatchers.IO).launch {
            val response = fService.getFavorites(userId!!).execute()
            if(response.code() == 200){
                var res = response.body()
                if(res != null)
                    fList = (res as MutableList<Favorite>)
            }
        }
    }

    private fun getTop3(){
        val oService = IntentApplication.retrofit.create(OrderService::class.java)
        oService.getTop3().enqueue(object : Callback<List<Int>> {
            override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                val res = response.body()
                if (response.code() == 200) {
                    if(res != null){
                        menuAdapter.topList = emptyList()
                        menuAdapter.topList = res
                        Log.d("TAG", "getTop3: data change start")
                        menuAdapter.notifyDataSetChanged()
                        Log.d("TAG", "getTop3: data change end")
                    }
                    else {
                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(activity, "TOP 정보를 가져올 수 없음!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                Log.d("TAG", "onFailure: 통신 오류!")
            }
        })
    }

    private fun getUserMenu(){
        val oService = IntentApplication.retrofit.create(OrderService::class.java)
        oService.getUserMenu(userId!!).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                val res = response.body()
                if (response.code() == 200) {
                    if(res != null){
                        menuAdapter.best = res
                        Log.d("TAG", "getTop3: data change start")
                        menuAdapter.notifyDataSetChanged()
                        Log.d("TAG", "getTop3: data change end")
                    }
                    else {
                        CoroutineScope(Dispatchers.Main).launch{
                            Toast.makeText(activity, "TOP 정보를 가져올 수 없음!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.d("TAG", "onFailure: 통신 오류!")
            }
        })
    }
}