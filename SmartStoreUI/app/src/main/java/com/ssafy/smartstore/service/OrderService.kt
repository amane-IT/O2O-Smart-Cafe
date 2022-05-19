package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.Order
import com.ssafy.smartstore.dto.OrderMap
import retrofit2.Call
import retrofit2.http.*

interface OrderService {
    // 주문 저장 & 저장된 order id 반환
    @POST("order")
    fun setOrder(@Body body: Order): Call<Int>

    // 주문 상세 내역 가져오기
    @GET("order/{orderId}")
    fun getOrder(@Path("orderId") orderId: Int): Call<List<Map<String, Object>>>

    // id에 해당하는 사용자의 최근 1개월간 주문 내역 반환 (반환 정보는 1차 주문번호 내림차순, 2차 주문 상세 내림차순)
    @GET("order/byUser")
    fun getLastMonthOrders(@Query("id") id: String): Call<List<OrderMap>>
}