package com.vaca.screenshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vaca.screenshot.service.ScreenRecordService

/**
 * @author by talon, Date on 19/6/23.
 * note:
 */
class MainActivity : AppCompatActivity() {
    private var mMediaProjectionManager: MediaProjectionManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //no status bar
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        );
        PathUtil.initVar(this)
        checkPermission(this) //检查权限
        mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    fun StartRecorder(view: View?) {
        createScreenCapture()
    }

    fun StopRecorder(view: View?) {
        val service = Intent(this, ScreenRecordService::class.java)
        stopService(service)
    }

    private fun createScreenCapture() {
        val captureIntent = mMediaProjectionManager!!.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Toast.makeText(this, "允许录屏", Toast.LENGTH_SHORT).show()
                val service = Intent(this, ScreenRecordService::class.java)
                service.putExtra("resultCode", resultCode)
                service.putExtra("data", data)
                startForegroundService(service)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "拒绝录屏", Toast.LENGTH_SHORT).show()
        }
    }

    fun fuck(view: View?) {
        ScreenRecordService.screenRecordService!!.startCapture()
    }

    companion object {
        private const val REQUEST_CODE = 1
        fun checkPermission(activity: AppCompatActivity?) {
            val checkPermission =
                (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO)
                        + ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.READ_PHONE_STATE
                )
                        + ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                        + ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ))
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                //动态申请
                ActivityCompat.requestPermissions(
                    activity, arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 123
                )
            }
        }
    }
}