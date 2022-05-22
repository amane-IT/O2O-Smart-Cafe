package com.ssafy.smartstore

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.smartstore.databinding.ActivityMainBinding
import com.ssafy.smartstore.dialog.CustomDialog
import org.altbeacon.beacon.*

private const val TAG = "HomeActivity_싸피"
class MainActivity : AppCompatActivity(), BeaconConsumer {

    // Beacon 관련 변수 ===================================
    // Beacon 장치 설정
    private lateinit var beaconManager: BeaconManager
    private val BEACON_UUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825"
    private val BEACON_MAJOR = "10004"
    private val BEACON_MINOR = "54480"

    private val CAFE_DISTANCE = 0.15
    private var CAFE_INFO_POPUP = false

    // Beacon의 Region(비콘 신호 영역?) 설정
    private val region = Region("ibeacon",
        Identifier.parse(BEACON_UUID),
        Identifier.parse(BEACON_MAJOR),
        Identifier.parse(BEACON_MINOR)
    )
//    val region = Region("altbeacon", null, null, null)

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var needBLERequest = true

    private val PERMISSIONS_CODE = 100

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    // ========================================================

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // StoreFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_frame) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)

        // notification
        // 1. FCM Token 수신
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "onCreate: FCM 토큰 얻기에 실패했습니다.", task.exception)
                return@OnCompleteListener
            }
            Log.d(TAG, "onCreate: token: ${task.result?:"task.result is null"}")
        })
        
        createNotificationChannel(channel_id, "Smart Store")

        // Beacon ===================================
        // beacon & bluetooth adapter, manager 설정
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"))
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        startScan()
        // ==========================================
    }

    // Beacon 관련 메서드 ===================================
    // Beacon Scan 시작
    private fun startScan() {
        Log.d(TAG, "startScan: ")

        if(!isEnableBLEServcie()) {
            requestEnableBLE()
            return
        }

        // 위치 정보 권한 허용 및 GPS Enable 여부 확인
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                PERMISSIONS_CODE
            )
        }

        Log.d(TAG, "startScan: bind 전")
        // Beacon Service bind
        beaconManager.bind(this)
        Log.d(TAG, "startScan: bind 완료")
    }

    // 블루투스 ON/OFF 여부 확인 및 켜도록 하는 함수
    private fun requestEnableBLE() {
        Log.d(TAG, "requestEnableBLE: ")
        val callBLEEnableInent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestBLEActivity.launch(callBLEEnableInent)
    }

    private val requestBLEActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 사용자의 블루투스 사용이 가능한지 확인
        if (isEnableBLEServcie()) {
            needBLERequest = false
            startScan()
        }
    }

    private fun isEnableBLEServcie(): Boolean {
        if (!bluetoothAdapter!!.isEnabled) {
            return false
        }
        return true
    }

    // 위치 정보 권한 요청 결과 콜백 함수
    // ActivityCompat.requestPermissions 실행 이후 실행
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_CODE -> {
                if(grantResults.isNotEmpty()) {
                    for((i, permission) in permissions.withIndex()) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //권한 획득 실패
                            Log.i(TAG, "$permission 권한 획득에 실패하였습니다.")
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect: ")
        beaconManager.addMonitorNotifier(object : MonitorNotifier {
            override fun didEnterRegion(region: Region?) {
                try {
                    Log.d(TAG, "didEnterRegion: 비콘 발견!")
                    beaconManager.startRangingBeaconsInRegion(region!!)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didExitRegion(region: Region?) {
                try {
                    Log.d(TAG, "didExitRegion: 비콘을 찾을 수 없음.")
                    beaconManager.stopRangingBeaconsInRegion(region!!)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun didDetermineStateForRegion(state: Int, region: Region?) {}
        })

        beaconManager.addRangeNotifier { beacons, region ->
            Log.d(TAG, "onBeaconServiceConnect: ")
            for (beacon in beacons) {
                if (isYourBeacon(beacon)) {
                    if (!CAFE_INFO_POPUP) {
                        runOnUiThread {
                            CAFE_INFO_POPUP = true
                            val dialog = CustomDialog(this@MainActivity)
                            dialog.start()
                        }
                    }
//                    if (beacon.distance > CAFE_DISTANCE) {
//                        CAFE_INFO_POPUP = false
//                    }
                }
            }
        }

        try {
            beaconManager.startMonitoringBeaconsInRegion(region)
        }
        catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun isYourBeacon(beacon: Beacon): Boolean {
        return (beacon.id2.toString() == BEACON_MAJOR &&
                beacon.id3.toString() == BEACON_MINOR &&
                beacon.distance <= CAFE_DISTANCE)
//        return beacon.distance <= CAFE_DISTANCE
    }

    override fun onDestroy() {
        beaconManager.stopMonitoringBeaconsInRegion(region)
        beaconManager.stopRangingBeaconsInRegion(region)
        beaconManager.unbind(this)
        super.onDestroy()
    }
    // ====================================================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id: String, name: String) {
        // Notification 수신을 위한 채널 추가
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(id, name, importance)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        // Notification Channel ID
        const val channel_id = "ssafy_channel"
    }
}