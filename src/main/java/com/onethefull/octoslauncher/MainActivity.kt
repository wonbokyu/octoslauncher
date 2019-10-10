package com.onethefull.octoslauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.JsonReader
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.StringReader

class MainActivity : AppCompatActivity() {

    private val ms = 3000
    private val TAG = "OCTOS"
    private val package_name = "com.onethefull.OctosDefault"
    private val code_name = "WONDERFUL"
    private val fileName = "avatarinfo.cfg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val uiOptions = window.decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = newUiOptions
    }

    override fun onResume() {


        super.onResume()
        val error_text: TextView = findViewById<TextView>(R.id.error_text) as TextView
        error_text.setText("")
        Handler().postDelayed({ avatarInfo("read") }, ms.toLong())


        runWolfService()
    }

    override fun onPause() {
        super.onPause()

    }

    /**
     *
     */
    fun runWolfService(){
//       val pm: PackageManager = packageManager.getServiceInfo(ComponentName.createRelative("",""))?.let {
//           it.
//       }



        var scenario: String=""
        var fileScenario = "octos_scenario_config.json"
        application.assets.open(fileScenario).apply {
            scenario = readBytes().toString(Charsets.UTF_8)
        }.close()

        try {
            scenario   = FileUtils.readFileToString(File("/sdcard/",fileScenario),"utf-8")
        } catch (e: Exception) {

            Log.e(TAG," e  ${e.message}")
        }

        Log.e(TAG," 런처에서 실행할 서비스를 바인드한다. ")
        if(scenario.isNotEmpty()) {
            try {
                var jsonObject = JSONObject(scenario)
                var keySERVICE_COMPONENTS = "SERVICE_COMPONENTS"
                if(jsonObject.has(keySERVICE_COMPONENTS)==true){
                    jsonObject.getJSONArray(keySERVICE_COMPONENTS)?.let {
                        for(i in 0..(it.length()-1)){
                            var item  = it.getJSONObject(i)
                            try {
                                Intent().let {
                                    it.component = ComponentName.createRelative(item.getString("PKG"),item.getString("CLS"))
                                    Log.i(TAG,"SERVICE NAME  ${it.component} " )
                                    bindService(it,serviceConnection, Context.BIND_AUTO_CREATE).let {
                                        Log.i(TAG,"start service UDPServerService ${it} " )
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG," e  ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG," e  ${e.message}")
            }
        }

//        Handler().postDelayed(Runnable {
//            try {
//                var intent: Intent = Intent()
//                intent.component = ComponentName.createRelative("com.google.cloud.android.speech","com.google.cloud.android.speech.SpeechService")
//                bindService(intent, connectService, Context.BIND_AUTO_CREATE)
//            } catch (e: Exception) {
//            }
//
//
//            try {
//                var intService = Intent()
//                intService.component = ComponentName.createRelative("com.wonderful.tts","com.kakao.sdk.newtone.sample.OctosTTSService")
//                startService(intService)
////                var intService = Intent()
////                intService.component = ComponentName.createRelative("com.wonderful.tts","com.kakao.sdk.newtone.sample.MainActivity")
////                startActivity(intService)
//            } catch (e: Exception) {
//            }
//        },1000*10)

    }




    var serviceConnection = object  : ServiceConnection{
        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.i(TAG,"...onServiceDisconnected " + p0.toString())
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

           Log.i(TAG,"...onServiceConnected " + p0.toString())
            Log.i(TAG,"...onServiceConnected " + p1.toString())
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    fun call_avatar(get_packageName: String){
        val pm = applicationContext.packageManager
        val intent:Intent? = pm.getLaunchIntentForPackage(get_packageName)
        intent?.addCategory(Intent.CATEGORY_LAUNCHER)
        if(intent!=null){
            applicationContext.startActivity(intent)
        }else{
            val error_text: TextView = findViewById<TextView>(R.id.error_text) as TextView
            error_text.setText(R.string.avatar_error)
        }
    }

    fun avatarInfo(check: String) {
        try {
            val pathname = Environment.getExternalStorageDirectory().toString()
            var avatar_File = File(pathname, fileName)

            if (check == "read" && avatar_File.isFile == true){
                val obj = JSONObject(avatar_File.readText())
                call_avatar(obj.getString("PACKAGE_NAME"))
            }else {
                // create a new file
                File(pathname).mkdirs()
                avatar_File.createNewFile()
                avatar_File.writeText(
                    "{\n\"PACKAGE_NAME\" : \"$package_name\",\n" +
                            "\"BUSINESS_CODE_NAME\" : \"$code_name\"\n}"
                )
                call_avatar(this.package_name)
            }
        }catch (e: Exception){
            Log.e(TAG, e.message)
            avatarInfo("no")
        }
    }
}
