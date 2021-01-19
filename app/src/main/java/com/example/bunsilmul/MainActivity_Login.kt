package com.example.bunsilmul


import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.usermgmt.LoginButton;
import com.kakao.util.exception.KakaoException;

import android.widget.*
import androidx.appcompat.app.AppCompatActivity
//import com.example.bunsilmul.databinding.ActivityMainLoginBinding
//import com.google.android.material.snackbar.Snackbar

import org.json.JSONObject;

//import java.util.HashMap;
import kotlin.collections.HashMap


class MainActivity_Login : AppCompatActivity() {
    private val TAG = MainActivity_Login::class.java.name

    lateinit var loggedInView: LinearLayout
    lateinit var loginButton: LoginButton
    lateinit var logoutButton: Button
    lateinit var imageView: ImageView

//    //Variable to hold service class name
//    val serviceClass = com.example.bunsilmul.ServiceLocation::class.java
//    //Initialize new Intent instance
//    val intent_service = Intent(this, serviceClass)

//    lateinit var binding: ActivityMainLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        println(Session.getCurrentSession().getAccessToken())

//        if (KakaoSDK.getAdapter() == null) {
//            KakaoSDK.init();
//        }
        setContentView(R.layout.activity_main_login)

        Intent(this, ServiceLocation::class.java).also { intent ->
            startService(intent)
        }

//        val toolBar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolBar)

//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_login)
//        loggedInView = findViewById<View>(R.id.logged_in_view) as LinearLayout
        loginButton = findViewById<View>(R.id.login_button) as LoginButton
//        logoutButton = findViewById<View>(R.id.logout_button) as Button
//        imageView = findViewById<View>(R.id.profile_image_view) as ImageView

//        logoutButton.setOnClickListener(object : View.OnClickListener{
//            override fun onClick(v: View?) {
//                Log.v("sdf","logout")
//                UserManagement.getInstance().requestLogout(object : LogoutResponseCallback() {
//                    override fun onCompleteLogout() {
//                        FirebaseAuth.getInstance().signOut()
//                        val handler = Handler(Looper.getMainLooper())
//                        handler.post( object: Runnable{
//                            override fun run() {
//                                updateUI()
//                                Log.v("asdf","sdf")
//                            }
//                        })
//                    }
//                })
//            }
//        })

        Session.getCurrentSession().addCallback(KakaoSessionCallback())

    }

    override fun onStart() {
        super.onStart()
//        Intent(this, ServiceLocation::class.java).also { intent ->
//            startService(intent)
//        }
//        updateUI()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }



    /**
     * Update UI based on Firebase's current user. Show Login Button if not logged in.
     */
//    private fun updateUI() {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null) {
//            binding.setCurrentUser(currentUser)
//            if (currentUser.photoUrl != null) {
//                Glide.with(this)
//                    .load(currentUser.photoUrl)
//                    .into(imageView)
//            }
//            loginButton.setVisibility(View.INVISIBLE)
//            loggedInView.visibility = View.VISIBLE
//            logoutButton.visibility = View.VISIBLE
//        } else {
//            loginButton.setVisibility(View.VISIBLE)
//            loggedInView.visibility = View.INVISIBLE
//            logoutButton.visibility = View.INVISIBLE
//        }
//    }

    /**
     * OnActivityResult() should be overridden for Kakao Login because Kakao Login uses startActivityForResult().
     */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)
    }

    /**
     *
     * @param kakaoAccessToken Access token retrieved after successful Kakao Login
     * @return Task object that will call validation server and retrieve firebase token
     */
    private fun getFirebaseJwt(kakaoAccessToken: String): Task<String> {
        val source = TaskCompletionSource<String>()
        val queue: RequestQueue = Volley.newRequestQueue(this)
        val url = "http://192.249.18.152:8000" + "/verifyToken"
////            resources.getString(R.string.validation_server_domain) + "/verifyToken"
        val validationObject: MutableMap<String, String> = HashMap()
        validationObject.put("token", kakaoAccessToken)
        val request: JsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, url, JSONObject(
            validationObject as kotlin.collections.Map<*, *>
        ), object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    try {
                        val firebaseToken = response.getString("firebase_token")
                        source.setResult(firebaseToken)
                    } catch (e: Exception) {
                        source.setException(e)
                    }
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Log.e(TAG, error.toString())
                    source.setException(error)
                }
            })
        {
            override fun getParams(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params.put("token",kakaoAccessToken)
                    return params
                }
        }
        queue.add(request)
        return source.task
    }

    /**
     * Session callback class for Kakao Login. OnSessionOpened() is called after successful login.
     */
    inner class KakaoSessionCallback : ISessionCallback {
        override fun onSessionOpened() {
            Log.d("OnsseionOpened","twice")
//            Toast.makeText(
//                this,
//                "Successfully logged in to Kakao. Now creating or updating a Firebase User.",
//                Toast.LENGTH_LONG
//            ).show()
            val accessToken: String = Session.getCurrentSession().getAccessToken()
            getFirebaseJwt(accessToken).continueWithTask { task ->
                Log.d("getFirebase","twice")
                val firebaseToken: String = task.result as String
                val auth = FirebaseAuth.getInstance()
                auth.signInWithCustomToken(firebaseToken)
            }.addOnCompleteListener(this@MainActivity_Login) { task ->
                if (task.isSuccessful()) {
                    //updateUI()
                    val intent = Intent(this@MainActivity_Login, MainActivity_Map::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    //                        Toast.makeText(
                    //                            getApplicationContext(),
                    //                            "Failed to create a Firebase user.",
                    //                            Toast.LENGTH_LONG
                    //                        ).show()
                    if (task.getException() != null) {
                        Log.e(TAG, task.getException().toString())
                    }
                }
            }
        }

        override fun onSessionOpenFailed(exception: KakaoException?) {
            if (exception != null) {
                Log.e(TAG, exception.toString())
            }
        }
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




}