package com.ssafy.smartstore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.ssafy.smartstore.dto.User
import com.ssafy.smartstore.databinding.ActivityJoinBinding
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// F02: 회원 관리 - 회원 정보 추가 회원 가입 - 회원 정보를 추가할 수 있다.
// F03: 회원 관리 - 회원 아이디 중복 확인 - 회원 가입 시 아이디가 중복되는지 여부를 확인할 수 있다.

private const val TAG = "JoinActivity_싸피"
class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private var isUsable = false
//    private val existId = "ssafy01"
    val userService = IntentApplication.retrofit.create(UserService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            etId.addTextChangedListener (object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    isUsable = false
                    imgbtnCheckId.setImageResource(R.drawable.check_mark)
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            CoroutineScope(Dispatchers.Main).launch {
                btnJoin.setOnClickListener {
                    joinUserInfo()
                }
            }
        }
    }

    // 아이디 중복 여부 체크
    fun checkId(view: View) {
        binding.apply {
            val id = etId.text.toString()

            if (id.isNotEmpty()) {
                userService.isLogin(id).enqueue(object : Callback<Boolean> {
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        if(response.code() == 200){
                            if(response.body() == true) {
                                isUsable = false
                                Toast.makeText(this@JoinActivity, "중복되는 아이디입니다.",
                                    Toast.LENGTH_SHORT).show()
                            } else{
                                isUsable = true
                                imgbtnCheckId.setImageResource(R.drawable.check)
                                Toast.makeText(this@JoinActivity, "사용 가능한 아이디입니다.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Log.d(TAG, "onFailure: 통신 실패")
                    }
                })
            }
            else {
                Toast.makeText(this@JoinActivity, "아이디를 입력하세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // id,name,pwd가 모두 등록되어 있는 경우 유저 정보 등록
    fun joinUserInfo() {
        binding.apply {
            val id = etId.text.toString()
            val pwd = etPwd.text.toString()
            val name = etNickname.text.toString()

            if (id.isNotEmpty() && pwd.isNotEmpty() && name.isNotEmpty()) {
                if (isUsable) {
                    CoroutineScope(Dispatchers.IO).launch {
                        var res = insertUserInfo(id, pwd, name)
                        CoroutineScope(Dispatchers.Main).launch {
                            if(res == 1){
                                Toast.makeText(this@JoinActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else{
                                Toast.makeText(this@JoinActivity, "회원가입 실패! ${res}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                else {
                    Toast.makeText(this@JoinActivity, "id를 체크하지 않았거나 중복이에요", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this@JoinActivity, "내용을 전부 채워주세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun insertUserInfo(id: String, pwd: String, name: String): Int {
        val response = userService.insertUser(User(id, name, pwd)).execute()
        val result = if(response.code() == 200){
            var res = response.body()
            if(res == true)
                return 1
            else
                return 2
        } else
            return response.code()
    }
}