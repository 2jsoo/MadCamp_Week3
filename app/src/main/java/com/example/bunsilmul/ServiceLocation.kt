package com.example.bunsilmul

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.249.18.152:8000/") // 마지막 / 반드시 들어가야 함
    .addConverterFactory(GsonConverterFactory.create()) // converter 지정
    .build() // com.example.bunsilmul.retrofit 객체 생성


class ServiceLocation : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null
    var locationManager: LocationManager? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {


        override fun handleMessage(msg: Message) {
            Log.d("SERVICE","HANDLER")
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                if (locationManager == null)
                    locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                try {
                    locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
                } catch (e: SecurityException) {
                    Log.e(TAG, "Fail to request location update", e)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Network provider does not exist", e)
                }

                try {
                    locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
                } catch (e: SecurityException) {
                    Log.e(TAG, "Fail to request location update", e)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "GPS provider does not exist", e)
                }


            } catch (e: InterruptedException) {
                // Restore interrupt status.
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    companion object {
        val TAG = "ServiceLocation"

        val INTERVAL = 0.toLong() // In milliseconds
        val DISTANCE = 0.toFloat() // In meters

        val locationListeners = arrayOf(
            get_location(LocationManager.GPS_PROVIDER),
            get_location(LocationManager.NETWORK_PROVIDER)
        )

        class get_location(provider: String) : LocationListener {
            val lastLocation = Location(provider)

            override fun onProviderDisabled(provider: String) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onLocationChanged(location: Location) {
                lastLocation.set(location)
                Log.d("locationchanged", "locationchanged")
                Log.d("locationchanged",""+location.latitude + location.longitude)
                var locations = locations(null, user_location(.0, .0))
                //서버로 location 전달
                locations.uid = FirebaseAuth.getInstance().currentUser?.uid
                locations.location.latitude = location.latitude
                locations.location.longitude = location.longitude

                val call = LocationApiObject.retrofitService.CreateLocation(locations)
                call.enqueue(object : retrofit2.Callback<wantmessage>{
                    override fun onFailure(call: Call<wantmessage>, t: Throwable) {
                        Log.d("LocationCreate","Fail")
                    }
                    override fun onResponse(call: Call<wantmessage>, response: retrofit2.Response<wantmessage>) {
                        if(response.isSuccessful){
                            Log.d("Location","Success")
                            response.body()?.let{
                                if(it.message == "success"){
                                    Log.d("LocationCreate","Success")
                                } else {
                                    Log.d("LocationCreate","error")
                                }
                            }
                        }
                        else{
                            Log.d("LocationCreate","response is error")
                        }
                    }
                })



            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
        }
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        Log.d("SERVICE","ONCREATE")
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()
            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        Log.d("SERVICE","START")

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            serviceHandler?.sendMessage(msg)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
        Log.d("SERVICE","DESTROY")
        Log.d("locationchanged","done")
//        Intent(this, ServiceLocation::class.java).also { intent ->
//            startService(intent)
//        }
//        val broadcastIntent = Intent()
//        broadcastIntent.action = "restartservice"
//        broadcastIntent.setClass(this, ServiceLocation::class.java)
//        this.sendBroadcast(broadcastIntent)
    }

//    override fun onTaskRemoved(rootIntent: Intent?) {
////        // TODO Auto-generated method stub
////        val restartService = Intent(
////            applicationContext,
////            this.javaClass
////        )
////        restartService.setPackage(packageName)
////        val restartServicePI = PendingIntent.getService(
////            applicationContext, 1, restartService,
////            PendingIntent.FLAG_ONE_SHOT
////        )
////        val alarmService =
////            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
////        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 0] =
////            restartServicePI
//
//        val intent = Intent("com.android.ServiceStopped")
//        sendBroadcast(intent)
//
//    }



}