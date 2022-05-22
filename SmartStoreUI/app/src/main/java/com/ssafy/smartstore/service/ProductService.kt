package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.dto.ProductDetail
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    // 전체 상품 목록 가져오기
    @GET("product")
    fun getAllProduct(): Call<List<Product>>

    // id에 해당하는 상품 정보 가져오기
    @GET("product/{productId}")
    fun getProduct(@Path("productId") productId: Int): Call<List<ProductDetail>>

    @GET("product/rating")
    fun getAvgRating(@Query("productId")productId: Int): Call<Double>
}