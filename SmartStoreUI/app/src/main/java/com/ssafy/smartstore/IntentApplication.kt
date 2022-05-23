package com.ssafy.smartstore

import android.app.Application
import com.ssafy.smartstore.dto.Grade
import com.ssafy.smartstore.fragment.Shopping
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.net.CookieManager

class IntentApplication: Application() {

    val localhost =
        "192.168.1.3"
        //"59.19.199.231"
    val SERVER_URL = "http://${localhost}:9999/rest/"

    override fun onCreate() {
        super.onCreate()

        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(CookieManager()))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(nullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        grade = Grade("", 0, 0, "")

        val theme = getSharedPreferences("theme", MODE_PRIVATE)
        if(theme.getInt("id", 0) == 0){
            val editor = theme.edit()
            editor.putInt("id", 1)
            editor.commit()
        }

    }

    companion object{
        lateinit var retrofit: Retrofit
        lateinit var client: OkHttpClient
        lateinit var grade: Grade
        var cntTmp = 0

        val shoppingList : MutableList<Shopping> = mutableListOf()
    }

    val nullOnEmptyConverterFactory = object : Converter.Factory() {
        fun converterFactory() = this
        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object : Converter<ResponseBody, Any?> {
            val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
            override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) nextResponseBodyConverter.convert(value) else null
        }
    }
}