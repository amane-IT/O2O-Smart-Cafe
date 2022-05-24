package com.ssafy.smartstore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.ssafy.smartstore.dto.User
import com.ssafy.smartstore.databinding.ActivityLoginBinding
import com.ssafy.smartstore.dto.Grade
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.*

// F04: 회원 관리 - 회원 로그인 - 추가된 회원 정보를 이용해서 로그인 할 수 있다. 로그아웃을 하기 전까지 앱을 실행시켰 을 때 로그인이 유지되어야 한다.
private const val TAG = "LoginActivity_싸피"
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var id: String
    private var birthDay = ""

    val userService = IntentApplication.retrofit.create(UserService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val theme = getSharedPreferences("theme", MODE_PRIVATE)
        val themeId = theme.getInt("id", 1)

        if(themeId == 1){
            setTheme(R.style.AppTheme)
        } else if(themeId == 2){
            setTheme(R.style.AppTheme_Blue)
        } else {
            setTheme(R.style.AppTheme_YB)
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPreference()
        binding.btnLogin.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                login()
            }
        }
    }

    private fun getPreference(){
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if(!prefs!!.getString("name", "정보 없음").equals("정보 없음")){
            binding.etId.setText(prefs!!.getString("id", ""))
            binding.etPwd.setText(prefs!!.getString("pwd", ""))
        }
    }

    private fun setPreference(recUser: User) {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("id", recUser.id)
        editor.putString("pwd", recUser.pass)
        editor.putString("name", recUser.name)
        editor.putInt("stamps", recUser.stamps)
        if(recUser.birthday != null){
            editor.putString("birthday", recUser.birthday)
            birthDay = recUser.birthday!!
        }
        editor.commit()
    }

    fun login() {
        binding.apply {
            // 사용자에게 아이디와 비밀번호를 전달받는다.
            id = etId.text.toString()
            val pwd = etPwd.text.toString()
            
            if (id.isNotEmpty() && pwd.isNotEmpty()) {
                // DB를 통해 해당 값으로 조회된 User 객체를 조회하고 반환 (id가 없으면 반환 X, 반환 받고는 비밀번호가 맞는지까지 체크)
                // 1 = 회원인 ID, 2 = 회원이 아닌 ID, 3 = 통신 실패
                var isJoin = checkID()
                CoroutineScope(Dispatchers.Main).launch {
                    if (isJoin == 2) {
                        Toast.makeText(this@LoginActivity, "없는 ID입니다!!!!", Toast.LENGTH_SHORT).show()
                    } else if(isJoin == 3){
                        Toast.makeText(this@LoginActivity, "통신 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        userService.getUserInfo(id).enqueue(object : Callback<Map<String, Object>> {
                            override fun onResponse(
                                call: Call<Map<String, Object>>,
                                response: Response<Map<String, Object>>
                            ) {
                                if (response.code() == 200) {
                                    val res = response.body() ?: mutableMapOf()
                                    val gson = Gson()
                                    val result = gson.fromJson(response.body()!!.get("user").toString(), User::class.java)
                                    val level = gson.fromJson(response.body()!!.get("grade").toString(), Grade::class.java)
                                    Log.d(TAG, "onResponse: ${result}")
                                    // result의 비번과 입력한 비번이 맞을 경우
                                    if (result.pass.equals(pwd)) {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Login 성공!!!!!!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // 정보가 모두 올바르다면 Shared Preference로 User 정보 저장 후, MainActivity 실행
                                        setPreference(result)

                                        // grade 정보 저장
                                        IntentApplication.grade = level

                                        // login api를 사용한다면 여기에 추가하기

                                        etId.text = null
                                        etPwd.text = null
                                        var intent = Intent(this@LoginActivity, SplashActivity::class.java)

                                        // 날짜
                                        val cal = Calendar.getInstance()
                                        val today = "${cal.get(Calendar.MONTH) + 1}_${cal.get(Calendar.DAY_OF_MONTH)}"
                                        if(!birthDay.equals(today))
                                            intent = Intent(this@LoginActivity, MainActivity::class.java)

                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "비밀번호가 다릅니다!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Map<String, Object>>, t: Throwable) {
                                Log.d(TAG, "onFailure: 통신 실패4, ${t.message}")
                            }
                        })
                    }
                }
            }
            else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@LoginActivity, "ID와 비밀번호를 모두 채워주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    // 회원가입 화면으로 이동
    fun join(view: View) {
        startActivity(Intent(this, JoinActivity::class.java))
    }

    fun checkID(): Int{
        val response = userService.isLogin(id).execute()
        if(response.code() == 200){
            if(response.body() == true)
                return 1
            else
                return 2
        } else{
            return 3
        }
    }
}