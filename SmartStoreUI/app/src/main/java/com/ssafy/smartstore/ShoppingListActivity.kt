package com.ssafy.smartstore

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.ssafy.smartstore.adapater.ItemAdapter
import com.ssafy.smartstore.fragment.Shopping
import com.ssafy.smartstore.databinding.ActivityShoppingListBinding
import com.ssafy.smartstore.dto.*
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat

// F06: 주문 관리 - 상품 주문 - 로그인한 사용자는 상품 상세 화면 에서 n개를 선정하여 장바구니에 등록할 수 있다. 로그인 한 사용자만 자기의 계정으로 구매를 처리할 수 있다.
// 장바구니 화면

private const val TAG = "ShoppingListActivity_싸피"
class ShoppingListActivity : AppCompatActivity() {

    companion object{
        var instance: ShoppingListActivity? = null
        private var adapter: ItemAdapter? = null

        fun getInstanceShopping(): ShoppingListActivity {
            if(instance == null)
                instance = ShoppingListActivity()
            return instance!!
        }
    }

    private lateinit var binding: ActivityShoppingListBinding
    private val orderService = IntentApplication.retrofit.create(OrderService::class.java)
    private val userService = IntentApplication.retrofit.create(UserService::class.java)

    private var total_price = 0
    private var total_cnt = 0
    private var cnt = 0

    // false = 매장
    // true = 테이크아웃
    private var outOrIn = false
    private var tableNum = "No.0"

    private lateinit var data: Product

    // NFC 관련 변수
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pIntent: PendingIntent
    private lateinit var filters:Array<IntentFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStore.setOnClickListener {
            binding.btnTakeout.setBackgroundResource(R.drawable.button_non_color)
            binding.btnStore.setBackgroundResource(R.drawable.button_color)
            outOrIn = false
        }

        binding.btnTakeout.setOnClickListener {
            binding.btnStore.setBackgroundResource(R.drawable.button_non_color)
            binding.btnTakeout.setBackgroundResource(R.drawable.button_color)
            outOrIn = true
        }

        // 새로운 preference 추가

        if (intent.getStringExtra("from").equals("order"))
            addPreference()

        else if(intent.getStringExtra("from").equals("home")){
            reorder(intent.getIntExtra("id", -1))
        }

        else
            getPreferences()

        binding.listOrder.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ItemAdapter(this)

        binding.btnOrder.setOnClickListener {
            order()
        }

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if(nfcAdapter == null)
            finish()

        val intent = Intent(this, ShoppingListActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        pIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val tag_filter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        filters = arrayOf(tag_filter)

    }

    override fun onResume() {
        super.onResume()
        // 삭제 버튼 클릭 시, 삭제
        val itemClickListener = object : ItemAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                total_cnt -= IntentApplication.shoppingList[position].quantity
                total_price -= IntentApplication.shoppingList[position].quantity * IntentApplication.shoppingList[position].price
                IntentApplication.shoppingList.removeAt(position)
                Log.d(TAG, "onItemClick: $position")
                getPreferences()
            }
        }
        adapter!!.objects = IntentApplication.shoppingList
        binding.listOrder.adapter = adapter
        adapter!!.onItemClickListener = itemClickListener

        // NFC foreground Mode
        nfcAdapter.enableForegroundDispatch(this, pIntent, filters, null)
    }

    // 포그라운드 모드 비활성화
    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this)
    }

    // 매개변수 intent = 새로운 intent
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("INFO", "onNewIntent: called...")

        val action = intent!!.action
        if(action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
            || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            processIntent(intent)
        }
    }

    // 장바구니에 상품 추가
    private fun addPreference(){
        data = intent.getSerializableExtra("data") as Product
        cnt = intent.getIntExtra("qty", 0)
        //shoppingList.add(Shopping(data.img.substring(0, data.img.length - 4), data.name, data.price, cnt))
        IntentApplication.shoppingList.add(Shopping(data.id, data.img.substring(0, data.img.length - 4), data.name, data.price, cnt))
        adapter!!.notifyDataSetChanged()
    }

    // 장바구니 갱신
    private fun getPreferences(){
        total_price = 0
        total_cnt = 0

        for(i in IntentApplication.shoppingList){
            total_cnt += i.quantity
            total_price += i.price * i.quantity
        }
        binding.tvQty.text = "총 ${total_cnt}개"
        binding.tvTotalPrice.text = "${total_price}원"
        adapter!!.notifyDataSetChanged()
    }

    // 주문하기
    private fun order() {
        val user = getSharedPreferences("prefs", MODE_PRIVATE)
        val userId = user.getString("id", "").toString()
        val detailList = mutableListOf<OrderDetail>()
        var totalCnt = 0

        for(i in IntentApplication.shoppingList){
            totalCnt += i.quantity
            detailList.add(OrderDetail(0, i.id, i.quantity))
        }

        CoroutineScope(Dispatchers.IO).launch {
            //order_table = NFC number

            var order_table = "1" // 0은 takeout
            Log.d(TAG, "order: order_table = ${order_table}")
            if(outOrIn){
                order_table = "0"
            } else {
                if (!tableNum.equals("No.0")) {
                    order_table = tableNum
                    Log.d(TAG, "order: order_table = ${order_table}")
                }
            }

            Log.d(TAG, "order: order_table = ${order_table} \n tableNum = ${tableNum}")

            if(!tableNum.equals("No.0") || order_table == "1"){
                val time = SimpleDateFormat("yyyy.MM.dd").format(System.currentTimeMillis())
                val order = Order(userId, order_table, time, 'N', detailList)
                val insertId = insertOrder(order)
                val updateStamp = updateStatus(userId)
                Log.d(TAG, "order: ${updateStamp}")

                if(insertId != -1){
                    IntentApplication.shoppingList.clear()

                    this.launch(Dispatchers.Main) {
                        val intent = Intent(this@ShoppingListActivity, MainActivity::class.java)
                        if(updateStamp == 1)
                            Toast.makeText(this@ShoppingListActivity, "주문이 완료되었습니다. \n" +
                                    " 스탬프가 ${totalCnt}개 적립되었습니다.", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this@ShoppingListActivity, "주문이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        startActivity(intent)
                        finish()
                    }
                } else{
                    this.launch(Dispatchers.Main) {
                        Toast.makeText(this@ShoppingListActivity, "주문을 실패했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            } else{
                this.launch(Dispatchers.Main) {
                    Toast.makeText(this@ShoppingListActivity, "테이크 아웃을 하시거나 테이블 태그를 찍어주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 주문하기 to Server
    private fun insertOrder(order: Order): Int{
        val response = orderService.setOrder(order).execute()
        val result = if(response.code() == 200){
            var res = response.body()
            if (res == null){
                -1
            } else
                res!!
        } else
            -1

        return result
    }

    // 스탬트 수 갱신 to Server
    private fun updateStatus(id: String): Int{
        val response = userService.updateStatus(id).execute()
        val result = if(response.code() == 200){
            var res = response.body()
            if(res == null){
                -1
            } else{
                val gson = Gson()
                val level = gson.fromJson(response.body()!!["grade"].toString(), Grade::class.java)
                Log.d(TAG, "updateStatus: ${level}")
                IntentApplication.grade = level
                1
            }
        } else
            response.code()
        return result
    }

    private fun processIntent(intent: Intent) {
        // 1. 인텐트에서 NdefMessage 배열 데이터를 가져온다
        val rawMsg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        // 2. Data 변환
        if(rawMsg != null) {
            val msgArr = arrayOfNulls<NdefMessage>(rawMsg.size)

            for (i in rawMsg.indices) {
                msgArr[i] = rawMsg[i] as NdefMessage?
            }

            // 3. NdefMessage에서 NdefRecode를 payload로 가져옴
            val recInfo = msgArr[0]!!.records[0]

            // Record type check : text, uri
            val data = recInfo.type
            val recType = String(data)

            if (recType.equals("T")) {
                tableNum = "No.${String(recInfo.payload, 3, recInfo.payload.size - 3)}"
                Log.d(TAG, "processIntent: $tableNum")
                Toast.makeText(this@ShoppingListActivity, "현재 테이블 번호는 ${tableNum} 입니다.", Toast.LENGTH_SHORT).show()
            }
            else { }
        }
    }

    fun reorder(id: Int){
        CoroutineScope(Dispatchers.IO).launch{
            val response = orderService.getOrder(id).execute()
            if(response.code() == 200){
                var res = response.body() ?: mutableListOf<Map<String, Object>>()
                var completed = "N"

                for(i in res){
                    val shop = Shopping(i.get("img").toString(), i.get("name").toString(), i.get("unitprice").toString().toDouble().toInt(), i.get("quantity").toString().toDouble().toInt())
                    completed = i.get("completed").toString()
                    IntentApplication.shoppingList.add(shop)
                }

                var price = 0
                for(i in IntentApplication.shoppingList){
                    price += i.price * i.quantity
                }

                adapter!!.objects = IntentApplication.shoppingList
                this.launch(Dispatchers.Main) {
                    adapter!!.notifyDataSetChanged()
                    getPreferences()
                }
            }
        }

    }
}