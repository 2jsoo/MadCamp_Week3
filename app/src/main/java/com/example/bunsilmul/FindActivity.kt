package com.example.bunsilmul

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
//
import android.graphics.Color
import android.graphics.Typeface
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
//
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FindActivity : AppCompatActivity(), MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private val LOG_TAG = "FindActivity"

    private lateinit var mapView: MapView
    private lateinit var mapViewContainer: ViewGroup

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)

    lateinit var mCurrentPhotoPath: String
    lateinit var photo_btn: ImageButton

    private var click = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find)

        mapView = MapView(this)
        mapViewContainer = findViewById<View>(R.id.find_map_view) as ViewGroup
        mapViewContainer.addView(mapView) // 지도를 띄움
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(36.373374, 127.359725), true);
        mapView.setZoomLevelFloat(3.0F, true)
        mapView.setMapRotationAngle(30F, true)
        mapView.setMapViewEventListener(this)
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving

        photo_btn = findViewById<ImageButton>(R.id.camera_button)
        photo_btn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setItems(arrayOf("카메라","앨범"), object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    when(which){
                        0 ->{
                            Log.d("카메라/앨범","카메라")
                            dispatchTakePictureIntent()
                        }
                        1 ->{
                            Log.d("카메라/앨범","앨범")
                            openGallery()

                        }
                    }
                }
            })
            builder.show()

        }

        val context = this
        val list = listOf("--선택--", "가방", "귀금속", "도서용품", "스포츠용품", "악기", "의류", "전자기기", "지갑", " 증명서", "컴퓨터", "카드", "현금", "휴대폰", "기타물품")
        val spinner: Spinner = findViewById(R.id.spinner)
        // initialize an array adapter for spinner
        val adapter: ArrayAdapter<String> = object: ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                list
        ){
            override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
            ): View {
                val view: TextView = super.getDropDownView(
                        position,
                        convertView,
                        parent
                ) as TextView
                // set item text bold
                view.setTypeface(view.typeface, Typeface.BOLD)
                // set selected item style
                if (position == spinner.selectedItemPosition){
                    view.background = ColorDrawable(Color.parseColor("#FAEBD7"))
                    view.setTextColor(Color.parseColor("#008000"))
                }
                return view
            }
        }
        // finally, data bind spinner with adapter
        spinner.adapter = adapter
        // spinner on item selected listener
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
            ) {}

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }

        //EditText
        var edit_text = findViewById(R.id.edit_text) as EditText
        if (edit_text != null){
            val string_ = edit_text.text.toString()
        }


        val register_btn = findViewById<Button>(R.id.register_button)
        register_btn.setOnClickListener {
            Log.d("분실물 위치",mapView.mapCenterPoint.mapPointGeoCoord.longitude.toString()+mapView.mapCenterPoint.mapPointGeoCoord.latitude.toString())
            //서버로 전달
            click = true
            val intent = Intent(this, MainActivity_Map::class.java)
            startActivity(intent)
            finish()
        }


    }

    override fun onDestroy() {
        mapView.visibility = View.INVISIBLE // Activity 꺼지면 전부 제거
        Log.d("find", "Destroy")
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if(click) {
            mapViewContainer.removeAllViews()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        click = true
        val intent = Intent(this, MainActivity_Map::class.java)
        startActivity(intent)
    }

    val REQUEST_IMAGE_CAPTURE = 1

    fun dispatchTakePictureIntent(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.bunsilmul.fileprovider",
                    it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }




    ///PHOTO&GALLERY RESULT
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val thumbnailBitmap = data?.extras?.get("data") as Bitmap // problem!! this bitmap is not original
            println("bitmap 가져옴")
        }

        try {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                val file = File(mCurrentPhotoPath)
                if (Build.VERSION.SDK_INT >= 29) {
                    val source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                    try {
                        val bitmapOrigin = ImageDecoder.decodeBitmap(source);
                        if (bitmapOrigin != null) {
                            println(bitmapOrigin)

                            val exifOrientation = ExifInterface(mCurrentPhotoPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                            val exifDegree = exifOrientationToDegrees(exifOrientation)
                            val bitmap = rotate(bitmapOrigin, exifDegree) // original bitmap
                            file.delete()
                            photo_btn.setImageBitmap(ReduceBitmap(bitmapOrigin))
                        }
                    }
                    catch (e:IOException) { e.printStackTrace(); } }
                else {
                    try {
                        val bitmapOrigin = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                        if (bitmapOrigin != null) {
                            println(bitmapOrigin)

                            val exifOrientation = ExifInterface(mCurrentPhotoPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                            val exifDegree = exifOrientationToDegrees(exifOrientation)
                            val bitmap = rotate(bitmapOrigin, exifDegree) // original bitmap
                            file.delete()
                            photo_btn.setImageBitmap(ReduceBitmap(bitmapOrigin))
                        }
                    }
                    catch (e :IOException ) { e.printStackTrace() }
                }
            }
            if(resultCode == RESULT_OK && requestCode == OPEN_GALLERY){
                var image : Uri? = data?.data
                var cr = contentResolver
                if(cr != null && image != null) {
                    val bitmap = when {
                        Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                                contentResolver, image
                        )
                        else -> {
                            val source = ImageDecoder.createSource(cr, image)
                            ImageDecoder.decodeBitmap(source)

                        }
                    }
                    photo_btn.setImageBitmap(bitmap)
                }
                else{ Log.d("Error", "Something Wrong") }
            } else{ Log.d("Error", "Something Wrong") }
        } catch (error :Exception ) { error.printStackTrace(); }


    }

    ///BITMAP --> STRING
    private fun imageToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imgBytes = byteArrayOutputStream.toByteArray()
        return encodeToString(imgBytes, DEFAULT)
    }

    ///PHOTO ROTATION ANGLE
    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    ///BITMAP ROTATE
    private fun rotate(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private val OPEN_GALLERY = 2

    ///OPEN GALLERY
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
//        startActivityForResult(intent, OPEN_GALLERY)
        startActivityForResult(Intent.createChooser(intent, "Select File"),OPEN_GALLERY)
    }

    private fun ReduceBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val heigth = bitmap.height

        return Bitmap.createScaledBitmap(bitmap, (width*0.3).toInt(), (heigth*0.3).toInt(),true)
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

}