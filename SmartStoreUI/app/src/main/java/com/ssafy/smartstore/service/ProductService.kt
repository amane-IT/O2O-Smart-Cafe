package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.Product
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path

interface ProductService {
    // 전체 상품 목록 가져오기
    @GET("product")
    fun getAllProduct(): Call<List<Product>>

    // id에 해당하는 상품 정보 가져오기
    @GET("product/{productId}")
    fun getProduct(@Path("productId") productId: Int): Call<Product>
}