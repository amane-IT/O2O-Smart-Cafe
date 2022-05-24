package com.ssafy.smartstore.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.ssafy.smartstore.R
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

private const val TAG = "MapInfoDialog_싸피"
class MapInfoDialog(context: Context) {
    private val ctx = context
    private val dialog = Dialog(ctx)

    private lateinit var tvCafeName: TextView

    private lateinit var btnSearchDir: Button
    private lateinit var btnCall: Button

    fun start(curPos: LatLng, marker: Marker) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_map_info)

        tvCafeName = dialog.findViewById(R.id.tv_cafe_name)
        btnSearchDir = dialog.findViewById(R.id.btn_search_direction)
        btnCall = dialog.findViewById(R.id.btn_call)

        tvCafeName.text = marker.title
        val phonenumber = "010-0000-0000" // 매장 전화 번호

        btnSearchDir.setOnClickListener {
            Log.d(TAG, "start: 길찾기 버튼")
            // TODO : SourceLocation과 DestinationLocation이 필요하다. (인자로 가져와라.)
            DisplayTrack(curPos, marker.position)
        }
        btnCall.setOnClickListener {
            Log.d(TAG, "start: 전화걸기 버튼")
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + phonenumber)))
        }
        dialog.show()
    }

    fun DisplayTrack(curPos: LatLng, destPos: LatLng) {

//        val uriString = "google.navigation:q=Seomyeon,+Pusan+Korea"
//        val uriString = "google.navigation:q=${destPos.latitude},${destPos.longitude}"
        val uriString = "https://www.google.com/maps/dir/?api=1&" +
                "origin=${curPos.latitude},${curPos.longitude}" +
                "&destination=${destPos.latitude},${destPos.longitude}" +
                "&travelmode=transit"
        /* "&travelmode=transit" : 대중교통 경로 확인 */
        /* 위 문장이 반드시 필요하다. 해외 기업이 국내 지도 기반 서비스를 못하도록 금지하고 있는데, */
        /* 따라서 도보, 차도는 제공하지 않는다. */
        /* 대신 대중교통 정보는 제공하고 있다. */

        // Initialize uri
        val uri = Uri.parse(uriString)
        // Initialize intent with action view
        val intent = Intent(Intent.ACTION_VIEW, uri)

        intent.setPackage("com.google.android.apps.maps")
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }
}
