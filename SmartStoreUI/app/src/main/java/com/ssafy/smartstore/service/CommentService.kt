package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.Comment
import retrofit2.Call
import retrofit2.http.*

interface CommentService {

    @GET("comment/byProduct")
    fun getComments(@Query("p_id") p_id : Int): Call<List<Comment>>

    @POST("comment")
    fun insert(@Body comment: Comment) : Call<Boolean>

    @PUT("comment")
    fun update(@Body comment: Comment) : Call<Boolean>

    @DELETE("comment/{id}")
    fun delete(@Path("id") id: Int) : Call<Boolean>
}