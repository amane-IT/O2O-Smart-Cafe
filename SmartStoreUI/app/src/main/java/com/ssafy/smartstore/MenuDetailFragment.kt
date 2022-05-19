package com.ssafy.smartstore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.databinding.FragmentMenuDetailBinding
import com.ssafy.smartstore.service.ProductService
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

class MenuDetailFragment : Fragment() {

    private lateinit var ctx: Context

    private lateinit var menuAdapter: MenuAdapter

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
        return FragmentMenuDetailBinding.inflate(inflater, container, false).apply {
            initAdapter()
            listMenu.apply {
                layoutManager = GridLayoutManager(ctx, 3)
                adapter = menuAdapter
            }
            
            val itemClickListener = object : MenuAdapter.OnItemClickListener{
                override fun onItemClick(view: View, position: Int) {
                    val intent = Intent(getActivity(), OrderActivity::class.java)
                    intent.putExtra("data", menuAdapter.listData[position])
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
                    }
                    else {
                        Toast.makeText(activity, "상품 정보를 가져올 수 없음!", Toast.LENGTH_SHORT).show()
                    }
                    menuAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Log.d("TAG", "onFailure: 통신 오류!")
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuDetailFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}