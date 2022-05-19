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
import com.ssafy.smartstore.dto.Order
import com.ssafy.smartstore.dto.OrderDetail
import com.ssafy.smartstore.dto.Product
import com.ssafy.smartstore.databases.Shopping
import com.ssafy.smartstore.databinding.ActivityShoppingListBinding
import com.ssafy.smartstore.service.OrderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat

// F06: 주문 관리 - 상품 주문 - 로그인한 사용자는 상품 상세 화면 에서 n개를 선정하여 장바구니에 등록할 수 있다. 로그인 한 사용자만 자기의 계정으로 구매를 처리할 수 있다.
// 장바구니 화면

private const val TAG = "ShoppingListActivity_싸피"
class ShoppingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingListBinding
    private val orderService = IntentApplication.retrofit.create(OrderService::class.java)

    private lateinit var adapter: ItemAdapter
    private val shoppingList : MutableList<Shopping> = mutableListOf()

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
                val remove = getSharedPreferences("item${position}", MODE_PRIVATE)
                val editor = remove.edit()
                total_cnt -= remove.getInt("cnt", 0)
                total_price -= (remove.getInt("price", 0) * remove.getInt("cnt", 0))
                editor.remove("cnt")
                editor.commit()
                Log.d(TAG, "onItemClick: $position")

                getPreferences()
                val adapter = ItemAdapter(this@ShoppingListActivity)
            }
        }
        adapter.objects = shoppingList
        binding.listOrder.adapter = adapter
        adapter.onItemClickListener = itemClickListener

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
        Log.d(TAG, "addPreference: ${System.currentTimeMillis()}")
        val item = getSharedPreferences("item${IntentApplication.cntTmp++}", MODE_PRIVATE)
        val editor = item.edit()
        editor.putInt("p_id", data.id)
        editor.putString("img", data.img.substring(0, data.img.length - 4))
        editor.putString("name", data.name)
        editor.putInt("price", data.price)
        editor.putInt("cnt", cnt)
        editor.commit()

        getPreferences()
    }

    // 장바구니 갱신
    private fun getPreferences(){
        shoppingList.clear()
        total_price = 0
        total_cnt = 0

        for(i in 0 until IntentApplication.cntTmp){
            val productTmp = getSharedPreferences("item${i}", MODE_PRIVATE)

            if(productTmp.getInt("cnt", 0) == 0) {
                Log.d(TAG, "getPreferences: item${i} 제거")
                continue
            }

            shoppingList.add(Shopping(  productTmp.getString("img", "").toString(),
                productTmp.getString("name", "").toString(),
                productTmp.getInt("price", 0),
                productTmp.getInt("cnt", 0)))
            Log.d(TAG, "getPreferences: ${productTmp.getString("name", "")}")
            total_cnt += productTmp.getInt("cnt", 0)
            total_price += (productTmp.getInt("price", 0) * productTmp.getInt("cnt", 0))
        }
        binding.tvQty.text = "총 ${total_cnt}개"
        binding.tvTotalPrice.text = "${total_price}원"
    }

    // 주문하기
    private fun order() {
        val user = getSharedPreferences("prefs", MODE_PRIVATE)
        val userId = user.getString("id", "").toString()
        val detailList = mutableListOf<OrderDetail>()

        for(i in 0 until IntentApplication.cntTmp){
            val detail = getSharedPreferences("item${i}", MODE_PRIVATE)
            val editor = detail.edit()
            if(detail.getInt("cnt", 0) != 0){
                Log.d(TAG, "order: ${System.currentTimeMillis()} , $userId")
                Log.d(TAG, "order: ${detail.getInt("p_id", -1)}")
                Log.d(TAG, "order: ${detail.getInt("cnt", -1)}")

                detailList.add(OrderDetail(0, detail.getInt("p_id", -1), detail.getInt("cnt", -1)))
            }
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

                if(insertId != -1){
                    for(i in 0 until IntentApplication.cntTmp) {
                        val detail = getSharedPreferences("item${i}", MODE_PRIVATE)
                        val editor = detail.edit()
                        editor.clear()
                        editor.apply()
                    }
                    IntentApplication.cntTmp = 0

                    this.launch(Dispatchers.Main) {
                        val intent = Intent(this@ShoppingListActivity, MainActivity::class.java)
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
}