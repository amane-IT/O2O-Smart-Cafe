package com.ssafy.smartstore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.ssafy.smartstore.databinding.ActivityMapBinding
import java.io.IOException
import java.util.*

// F16 - 회원 위치 알림 (Google map)
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding

    private val UPDATE_INTERVAL = 1000
    private val FASTEST_UPDATE_INTERVAL = 500

    private var mMap: GoogleMap? = null // 구글 맵 객체
    private var currentMarker: Marker? = null // 마커
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient // 효율적인 위치정보 제공을 위한 api
    private lateinit var locationRequest: LocationRequest // 위치 업데이트 요청
    private lateinit var mCurrentLocation: Location
    private lateinit var currentPosition: LatLng

    private val cafeLatLng = LatLng(35.175595791074, 129.07040766977)
    private var cafeMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val theme = getSharedPreferences("theme", MODE_PRIVATE)
        val themeId = theme.getInt("id", 1)

        if(themeId == 1){
            setTheme(R.style.AppTheme)
        } else if(themeId == 2){
            setTheme(R.style.AppTheme_Green)
        } else {
            setTheme(R.style.AppTheme_YB)
        }

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 위치 업데이터 요청 객체 생성
        locationRequest = LocationRequest.create().apply { 
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL.toLong()
            fastestInterval = FASTEST_UPDATE_INTERVAL.toLong()
            smallestDisplacement = 10.0f
        }
        
        // builder 생성 + locationRequest와 연결
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        
        // 효율적 위치 정보 제공 api 객체 생성 및 map fragment 설정
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        if (checkPermission()) {
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // 구글 맵이 띄워질 때 실행
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        // 위치 퍼미션이 있는지 확인 후,
        if (checkPermission()) {
            startLocationUpdates()
        } else {
            // 권한이 없다면 권한 요청
            val permissionListener = object : PermissionListener {
                override fun onPermissionGranted() {
                    startLocationUpdates()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(this@MapActivity, "위치 권한이 거부됨.", Toast.LENGTH_SHORT).show()
                }
            }
            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[설정]에서 위치 접근 권한을 부여해야 합니다.")
                .setPermissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check()
        }

        // 구글맵에 "현재 위치 버튼"을 보일 것인가를 지정
//        if (mMap != null) {
//            mMap!!.uiSettings.isMyLocationButtonEnabled = true
//        }

        // 카페 위치 마커 표시
        val markerOptions = MarkerOptions()
        markerOptions.apply {
            position(cafeLatLng)
            draggable(true)
        }
        cafeMarker = mMap!!.addMarker(markerOptions)
    }

    // 위치를 업데이트
    private fun startLocationUpdates() {
        // 위치 서비스 활성화 체크
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        }
        else {
            if (checkPermission()) {
                mFusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()!!
                )
                
                if (mMap != null) {
                    // 현재 위치 찾는 버튼 활성화
                    mMap!!.isMyLocationEnabled = true
                    // 줌 버튼 활성화
                    mMap!!.uiSettings.isZoomControlsEnabled = true
                }
            }
        }
    }

    // 위치정보 요청시 호출
    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                
                // 현재 위치는 locationList[size - 1], 위도경도를 얻어와서 저장
                currentPosition = LatLng(location.latitude, location.longitude)

                // 현재 위치 정보를 currentPosition에 저장
                setCurrentLocation(location)
                mCurrentLocation = location

                setDistanceInfo()
            }
        }

        private fun setDistanceInfo() {
            var R = 3958.8 // Radius of the Earth
            var rCafeLat = cafeLatLng.latitude * (Math.PI / 180)
            var rCurLat = currentPosition.latitude * (Math.PI / 180)
            var diffLat = rCurLat - rCafeLat
            var diffLon = (currentPosition.longitude - cafeLatLng.longitude) * (Math.PI / 180)

            var d = 2 * R * Math.asin(Math.sqrt(Math.sin(diffLat/2)*Math.sin(diffLat/2)
                + Math.cos(rCafeLat)*Math.cos(rCurLat)*Math.sin(diffLon/2)*Math.sin(diffLon/2)))
            val mileToMeter = 1609.34

            binding.tvDist.text = "카페까지의 거리는 ${String.format("%.2f", d * mileToMeter / 1000)}km 입니다."
        }
    }

    // 현재 위치를 표기해주는 함수
    private fun setCurrentLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
        mMap!!.animateCamera(cameraUpdate)
    }

    // 현재 위치 정보의 상세 주소를 반환
    private fun getCurrentAddress(latlng: LatLng): String {
        //지오코더: GPS를 주소로 변환
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }

        return if (addresses == null || addresses.isEmpty()) {
            Toast.makeText(this, "주소 발견 불가", Toast.LENGTH_LONG).show()
            "주소 발견 불가"
        } else {
            val address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }

    /******** 위치서비스 활성화 여부 check *********/
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { _, _ ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }
        builder.setNegativeButton("취소"
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    // GPS 혹은 Network가 이용 가능한지 여부 반환
    private fun checkLocationServicesStatus(): Boolean {
        val locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // 권한 허용 여부 반환
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED
                && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }
}

// 1. 회원의 GPS 정보와 매장의 위치로
// 3. 지도에 사용자 위치와 매장의 위치를 나타낸다.

// 2. 사용자에게 매장과의 거리를 제공하고, (이를 OrderFragment에 표기)
