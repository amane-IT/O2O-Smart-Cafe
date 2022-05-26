package com.ssafy.smartstore

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ssafy.smartstore.dto.User
import com.ssafy.smartstore.databinding.ActivityJoinBinding
import com.ssafy.smartstore.dialog.DateDialog
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

// F02: 회원 관리 - 회원 정보 추가 회원 가입 - 회원 정보를 추가할 수 있다.
// F03: 회원 관리 - 회원 아이디 중복 확인 - 회원 가입 시 아이디가 중복되는지 여부를 확인할 수 있다.

private const val TAG = "JoinActivity_싸피"
class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private var isUsable = false

    val userService = IntentApplication.retrofit.create(UserService::class.java)

    val myCanlendar = Calendar.getInstance()
    var birthday: String? = null


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

        instance = this

        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 생일
        binding.apply {
            edBirthday.inputType = InputType.TYPE_NULL

            edBirthday.setOnClickListener {
                Log.d(TAG, "onCreate: 생일 터치")

                val dialog = DateDialog(this@JoinActivity)
                dialog.start()
            }

            CoroutineScope(Dispatchers.Main).launch {
                btnJoin.setOnClickListener {
                    confirmBirthday()
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

    fun confirmBirthday(){
        binding.apply {
            Log.d(TAG, "confirmBirthday: $birthday")
            if(edBirthday.text.isNotBlank()){
                val tmp = edBirthday.text.split("월 ")
                birthday = tmp[0] + "_" + tmp[1].substring(0, tmp[1].length - 1)
            }
            Log.d(TAG, "confirmBirthday: $birthday")
            if(birthday == null){
                Log.d(TAG, "joinUserInfo: 없음")
                val builder = AlertDialog.Builder(this@JoinActivity, R.style.AppCompatAlertDialog)
                builder.setTitle("생일을 입력하시지 않으셨습니다.")
                    .setMessage("생일을 입력하시지 않으면 생일 관련 쿠폰이나 이벤트에 참여하시지 못하는 데 괜찮으신가요?")
                    .setPositiveButton("확인",
                        DialogInterface.OnClickListener { dialog, which ->
                            Log.d(TAG, "confirmBirthday: 눌림")
                            joinUserInfo()
                            dialog.dismiss()
                        }
                    ) .setNegativeButton("취소",
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        }
                    )
                builder.show()
            } else{
                joinUserInfo()
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
                        var res = insertUserInfo(id, pwd, name, birthday)
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

    private fun insertUserInfo(id: String, pwd: String, name: String, birthday: String?): Int {
        val response = userService.insertUser(User(id, name, pwd, birthday)).execute()
        val result = if (response.code() == 200) {
            var res = response.body()
            if (res == true)
                return 1
            else
                return 2
        } else
            return response.code()
    }

    fun setBirthdayText(birthday: String){
        binding.edBirthday.setText(birthday)
    }

    companion object{
        private var instance: JoinActivity? = null

        fun getInstanceJoin(): JoinActivity{
            if(instance == null)
                instance = JoinActivity()
            return instance!!
        }
    }
}