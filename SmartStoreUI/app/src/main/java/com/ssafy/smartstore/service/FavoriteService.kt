package com.ssafy.smartstore.service

import com.ssafy.smartstore.dto.Favorite
import retrofit2.Call
import retrofit2.http.*

interface FavoriteService {
    @GET("favorite/byUser")
    fun getFavorites(@Query("user_id") user_id : String): Call<List<Favorite>>

    @POST("favorite")
    fun insert(@Body favorite: Favorite) : Call<Favorite>

    @DELETE("favorite/{id}")
    fun delete(@Path("id") id: Int) : Call<Boolean>
}