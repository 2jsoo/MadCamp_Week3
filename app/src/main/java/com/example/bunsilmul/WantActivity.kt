package com.example.bunsilmul

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class WantActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private val LOG_TAG = "MainActivity"
    private lateinit var mapView: MapView
    private lateinit var mapViewContainer: ViewGroup
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wantactivity)
        mapView = MapView(this)
        mapViewContainer = findViewById<View>(R.id.map_view_want) as ViewGroup
        mapViewContainer.addView(mapView) // 지도를 띄움
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(36.373374, 127.359725), true);
        mapView.setZoomLevelFloat(3.0F, true)
        mapView.setMapRotationAngle(30F, true)
        mapView.setMapViewEventListener(this)


        val back_button_w = findViewById<ImageButton>(R.id.back_button_w)
        back_button_w.setOnClickListener{
            val intentback = Intent(this, MainActivity_Map::class.java)
            startActivity(intentback)
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mapViewContainer.removeAllViews() // Activity 꺼지면 전부 제거
    }
    //
    override fun onPause(){
        super.onPause()
        mapViewContainer.removeAllViews()
    }

//    override fun onResume(){
//        super.onResume()
//        mapViewContainer.removeAllViews()
//    }

//    override fun onStop() {
//        super.onStop()
//        mapViewContainer.removeAllViews()
//    }

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

    var time3: Long = 0
    override fun onBackPressed() {
        val time1 = System.currentTimeMillis()
        val time2 = time1 - time3
        if (time2 in 0..2000) {
            finish()
        }
        else {
            time3 = time1
            Toast.makeText(applicationContext, "한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show()
        }
    }

}
