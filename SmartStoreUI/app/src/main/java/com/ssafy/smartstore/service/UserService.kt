package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {

    // 사용자 정보 추가
    @POST("user")
    fun insertUser(@Body user: User) : Call<Boolean>

    // 사용자의 정보와 함께 사용자의 주문 내역, 사용자 등급 정보를 반환
    @POST("user/info")
    fun getUserInfo(@Query("id") id : String) : Call<Map<String, Object>>

    // request parameter로 전달된 id가 이미 사용중인지 반환
    @GET("user/isUsed")
    fun isLogin(@Query("id") id: String) : Call<Boolean>

    // 로그인 처리 후, 성공적으로 로그인이 되었다면 loginId라는 쿠키를 내려보낸다
    @POST("/user/login")
    fun login(@Body user : User) : Call<User>

}