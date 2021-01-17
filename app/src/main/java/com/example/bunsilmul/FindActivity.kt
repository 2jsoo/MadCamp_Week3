package com.example.bunsilmul

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
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
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class bunsilmul(
    var _id: String?,
    var uid: String?,
    var category: String?,
    var information: String?,
    var photo: String?,
    var latitude: Double?,
    var longitude: Double?
)

data class message(
    var message: String,
    var _id: String
)
private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.249.18.133:8080/") // 마지막 / 반드시 들어가야 함
    .addConverterFactory(GsonConverterFactory.create()) // converter 지정
    .build() // retrofit 객체 생성


object BunsilmulApiObject {
    val retrofitService: BunsilmulInterface by lazy {
        retrofit.create(BunsilmulInterface::class.java)
    }
}




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

    private var input_category = false
    private var input_information = false
    private var input_photo = false

    private lateinit var register_btn: Button


    private var bunsilmul = bunsilmul(null,null, null, null, null, null, null)

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
            ) {
                if(position == 0 ){
                    input_category = false
                    register_btn.isEnabled =false
                }else{
                    input_category = true
                    bunsilmul.category = list[position]
                    if(input_category && input_information && input_photo){
                        register_btn.isEnabled = true
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }

        //EditText
        var edit_text = findViewById(R.id.edit_text) as EditText
        if (edit_text != null){
            val string = edit_text.text.toString()
            Log.d("정보",string)
        }

        edit_text.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s==null){
                    input_information = false
                    register_btn.isEnabled = false
                }else if(s.isEmpty()){
                    input_information = false
                    register_btn.isEnabled = false
                } else{
                    input_information = true
                    bunsilmul.information = s.toString()
                    if(input_category && input_information && input_photo){
                        register_btn.isEnabled = true
                    }

                }
            }

        })


        register_btn = findViewById<Button>(R.id.register_button)
        register_btn.isEnabled  = false
        register_btn.setOnClickListener {
            Log.d("분실물 위치",mapView.mapCenterPoint.mapPointGeoCoord.longitude.toString()+mapView.mapCenterPoint.mapPointGeoCoord.latitude.toString())
            //서버로 전달
            bunsilmul.uid = FirebaseAuth.getInstance().currentUser?.uid
            bunsilmul.latitude = mapView.mapCenterPoint.mapPointGeoCoord.latitude
            bunsilmul.longitude = mapView.mapCenterPoint.mapPointGeoCoord.longitude
            register_btn.isEnabled = false
            val call = BunsilmulApiObject.retrofitService.CreateBunsilmul(bunsilmul)
            call.enqueue(object: retrofit2.Callback<message> {
                override fun onFailure(call: Call<message>, t: Throwable) {
                    Log.d("BunsilmulCreate","Fail")
                    register_btn.isEnabled = true
                }
                override fun onResponse(call: Call<message>, response: retrofit2.Response<message>) {
                    if(response.isSuccessful){
                        response.body()?.let{
                            if(it.message == "success"){
                                Log.d("BunsilmulCreate","Success")
                                click = true
                                bunsilmul._id = it._id
                                Log.d("ID",it._id)
                                val intent = Intent(this@FindActivity, MainActivity_Map::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Log.d("BunsilmulCreate","error")
                                register_btn.isEnabled = true
                            }
                        }
                    }
                    else{
                        Log.d("BunsilmulCreate","response is error")
                        register_btn.isEnabled = true
                    }
                }
            })

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
                            bunsilmul.photo = imageToString(bitmapOrigin)
                            input_photo = true
                            if(input_category && input_information && input_photo){
                                register_btn.isEnabled = true
                            }
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
                            photo_btn.setImageBitmap(ReduceBitmap(bitmap))
                            bunsilmul.photo = imageToString(bitmap)
                            input_photo = true
                            if(input_category && input_information && input_photo){
                                register_btn.isEnabled = true
                            }
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
                    bunsilmul.photo = imageToString(bitmap)
                    photo_btn.setImageBitmap(bitmap)
                    input_photo = true
                    if(input_category && input_information && input_photo){
                        register_btn.isEnabled = true
                    }
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

        return Bitmap.createScaledBitmap(bitmap, (width*0.1).toInt(), (heigth*0.1).toInt(),true)
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