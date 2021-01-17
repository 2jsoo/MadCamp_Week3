package com.example.bunsilmul

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.249.18.133:8080/") // 마지막 / 반드시 들어가야 함
    .addConverterFactory(GsonConverterFactory.create()) // converter 지정
    .build() // retrofit 객체 생성

class MainActivity_Map : AppCompatActivity(), MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener {

    private val LOG_TAG = "MainActivity"

    private lateinit var mapView: MapView
    private lateinit var mapViewContainer: ViewGroup

    private var Markers: ArrayList<MapPOIItem> = arrayListOf()
    private var bunsilmulMarkers: ArrayList<bunsilmul> = arrayListOf()
    private val bunsilmulMap: MutableMap<MapPOIItem, bunsilmul> = mutableMapOf()

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)

    private var pause = false

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map)

        mapView = MapView(this)
        mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView) // 지도를 띄움
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(36.373374, 127.359725), true);
//        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
        mapView.setZoomLevelFloat(3.0F, true)
        mapView.setMapRotationAngle(30F, true)
        mapView.setMapViewEventListener(this)
        if(!checkLocationServicesStatus()){ // GPS 꺼져있음
            showDialogForLocationServiceSetting()
        }else{ // GPS 켜져있으면 RUNTIME PERMISSION을 요구함.
            checkRunTimePermission()
        }

        val find_button = findViewById<Button>(R.id.find)
        find_button.setOnClickListener {
//            mapViewContainer.removeAllViews()
            pause = true
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
            finish()
        }

        val want_button = findViewById<Button>(R.id.want)
        want_button.setOnClickListener{
            pause = true
            val intentwant = Intent(this, WantActivity::class.java)
            startActivity(intentwant)
            finish()
        }

        val setting_button = findViewById<Button>(R.id.setting)
        setting_button.setOnClickListener{
            pause = true
            val intentsetting = Intent(this, Activity_Settings::class.java)
            startActivity(intentsetting)
            finish()
        }

//        val Marker1 = MapPOIItem()
//        Marker1.itemName = "분실물"
//
//        Marker1.tag = 0
//        Marker1.mapPoint= MapPoint.mapPointWithGeoCoord(36.373374, 127.359725)
//        Marker1.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
//        Marker1.customImageResourceId = R.drawable.question_mark// 마커 이미지.
//        Marker1.isCustomImageAutoscale = false// hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//        Marker1.setCustomImageAnchor(0.5f, 1.0f) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
////        mapView.addPOIItem(Marker1)
//
//        val Marker2 = MapPOIItem()
//        Marker2.itemName = "분실물"
//
//        Marker2.tag = 0
//        Marker2.mapPoint= MapPoint.mapPointWithGeoCoord(36.369476318359375, 127.36253356933594)
//        Marker2.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
//        Marker2.customImageResourceId = R.drawable.question_mark// 마커 이미지.
//        Marker2.isCustomImageAutoscale = false// hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//        Marker2.setCustomImageAnchor(0.5f, 1.0f) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
////        mapView.addPOIItem(Marker2)
//        mapView.addPOIItems(arrayOf(Marker1, Marker2))

        mapView.setCalloutBalloonAdapter(object: CalloutBalloonAdapter{
            private val mCalloutBalloon = layoutInflater.inflate(R.layout.custom_marker, null)

            override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {
                //activity intent
                //acitivity에 정보 전달 필요
//                val intent = Intent(this@MainActivity, bunsilmulActivity::class.java)
//                startActivity(intent)
//                mCalloutBalloon.findViewById<TextView>(R.id.desc).text = "선택"
                return mCalloutBalloon
            }


            override fun getCalloutBalloon(p0: MapPOIItem?): View {
                mCalloutBalloon.findViewById<TextView>(R.id.title).text = p0?.getItemName()
//                mCalloutBalloon.findViewById<TextView>(R.id.desc).text = "Custom CalloutBalloon"
                return mCalloutBalloon
            }
        })

        mapView.setPOIItemEventListener(this)

//        mapView.addPOIItem(Marker1)

        mHandler = Handler()
        GetBunsilmulItems()
//        val marker: MapPOIItem = MapPOIItem()
//        marker.setItemName("Default Marker")
//        marker.setTag(0)
//        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(36.373374, 127.359725))
//        marker.setMarkerType(MapPOIItem.MarkerType.BluePin) // 기본으로 제공하는 BluePin 마커 모양.
//
//        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin) // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//
//
//        mapView.addPOIItem(marker)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("전환", "Destroy")
        mapViewContainer.removeAllViews() // Activity 꺼지면 전부 제거

    }

    override fun onStop() {
        super.onStop()
//        mapViewContainer.removeAllViews()
    }

    override fun onPause() {
        super.onPause()
        Log.d("전환", "Pause")
        if(pause) {
            mapViewContainer.removeAllViews()
        }
//        mapView.visibility = View.INVISIBLE

    }


    override fun onResume() {
        Log.d("전환", "Resume")
        if(pause) {
            Log.d("맵","Resume")
//            val newmapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
//            mapViewContainer.visibility = View.GONE
//            mapView.visibility = SurfaceView.VISIBLE // 지도를 띄움
//            mapViewContainer.addView(mapView)
//            mapView.visibility = View.VISIBLE
//            mapViewContainer.requestLayout()
//            mapViewContainer.invalidate()
//            Log.d("childern", mapViewContainer.children.toString())
//            if(mapViewContainer.getChildAt(mapViewContainer.indexOfChild(mapView)) == mapView) {
//                Log.d("보일까?", mapView.isVisible.toString())
//                Log.d("VIEW", mapViewContainer.childCount.toString())
//            }
        }
        super.onResume()
    }


    override fun onCurrentLocationUpdate(p0: MapView?, currentLocation: MapPoint?, accuracyInMeters: Float) {
        val mapPointGeo = currentLocation?.mapPointGeoCoord
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo?.latitude, mapPointGeo?.longitude, accuracyInMeters))
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size){
            var check_result = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }

            if (check_result){
                mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
            }


        }
    }

    fun checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                this@MainActivity_Map,
                Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity_Map,
                            REQUIRED_PERMISSIONS[0]
                    )
            ) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@MainActivity_Map, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG)
                        .show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                        this@MainActivity_Map, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE
                )
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                        this@MainActivity_Map, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity_Map)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
                """
                앱을 사용하기 위해서는 위치 서비스가 필요합니다.
                위치 설정을 수정하시겠습니까?
                """.trimIndent()
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent =
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        })
        builder.setNegativeButton("취소",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->                 //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }

        }
    }

    fun checkLocationServicesStatus(): Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewInitialized(p0: MapView?) {
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }

    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
    }
    override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        Log.d("Balloon","Touched")
        val intent = Intent(this@MainActivity_Map, bunsilmulActivity::class.java)


        bunsilmulMap.get(p1)?._id?.let{intent.putExtra("id", it)}
        bunsilmulMap.get(p1)?.category?.let{intent.putExtra("category", it)}
        bunsilmulMap.get(p1)?.information?.let{intent.putExtra("information", it)}
        bunsilmulMap.get(p1)?.uid?.let{intent.putExtra("uid",it)}

        bunsilmulMap.get(p1)?._id?.let {Log.d("ID", it)}
        bunsilmulMap.get(p1)?.category?.let {Log.d("CATEGORY", it)}
        bunsilmulMap.get(p1)?.information?.let {Log.d("INFORMATION", it)}

        startActivity(intent)

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
    }
    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        Log.d("MARKER","TOUCHED")
    }

    var time3: Long = 0
    override fun onBackPressed() {
        val time1 = System.currentTimeMillis()
        val time2 = time1 - time3
        if (time2 in 0..2000) {
            moveTaskToBack(true) // 태스크를 백그라운드로 이동
            finishAndRemoveTask() // 액티비티 종료 + 태스크 리스트에서 지우기
            System.exit(0)
        }
        else {
            time3 = time1
            Toast.makeText(applicationContext, "한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
        }
    }

    fun GetBunsilmulItems(){
        val call = BunsilmulApiObject.retrofitService.GetBunsilmul()
        call.enqueue(object: retrofit2.Callback<Array<bunsilmul>> {
            override fun onFailure(call: Call<Array<bunsilmul>>, t: Throwable) {
                Log.d("BunsilmulCreate","Fail")
            }
            override fun onResponse(call: Call<Array<bunsilmul>>, response: retrofit2.Response<Array<bunsilmul>>) {
                if(response.isSuccessful){
                    response.body()?.let{
                        val runnable = object: Runnable{
                            override fun run() {
                                var i = 0
                                for (item in it) {
                                    Log.d("분실물","GET")
                                    val Marker = MapPOIItem()
                                    Marker.itemName = "분실물"
                                    Marker.tag = i
                                    Marker.mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude!!, item.longitude!!)
                                    Marker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.
                                    Marker.customImageResourceId = R.drawable.question_mark// 마커 이미지.
                                    Marker.isCustomImageAutoscale = false// hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    Marker.setCustomImageAnchor(0.5f, 1.0f) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                    mapView.addPOIItem(Marker)
                                    Markers.add(Marker)
                                    bunsilmulMarkers.add(item)
                                    bunsilmulMap.put(Marker, item) // bunsilmul's photo is null
                                    i++
                                    item._id?.let{Log.d("id", it)}
                                }
                            }
                        }
                        mHandler.post(runnable)
                    }
                }
                else{
                    Log.d("BunsilmulCreate","response is error")
                }
            }
        })
    }

//    private fun getHashKey() {
//        var packageInfo: PackageInfo? = null
//        try {
//            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//        } catch (e: PackageManager.NameNotFoundException) {
//            e.printStackTrace()
//        }
//        if (packageInfo == null) Log.d("KeyHash", "KeyHash:null")
//        for (signature in packageInfo!!.signatures) {
//            try {
//                val md: MessageDigest = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//            } catch (e: NoSuchAlgorithmException) {
//                Log.d("KeyHash", "Unable to get MessageDigest. signature=$signature", e)
//            }
//        }
//    }
}