package com.vaca.screenshot.utils

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.vaca.screenshot.service.ScreenRecordService

object CaptureUtils {
    var mResultCode = 0
    var mResultData: Intent? = null
    lateinit var launcher: ActivityResultLauncher<Intent>
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private lateinit var activity:AppCompatActivity
    fun registerCapture(activity:AppCompatActivity){
        this.activity=activity
        launcher=activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    try {
                        Toast.makeText(activity, "允许录屏", Toast.LENGTH_SHORT).show()
                        val service = Intent(activity, ScreenRecordService::class.java)
                        CaptureUtils.mResultCode = result.resultCode
                        CaptureUtils.mResultData = result.data
                        activity.startForegroundService(service)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(activity, "拒绝录屏", Toast.LENGTH_SHORT).show()
                }
            })

        mMediaProjectionManager =
           activity.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    }

    fun createScreenCapture() {
        val captureIntent = mMediaProjectionManager!!.createScreenCaptureIntent()
        launcher.launch(captureIntent)
    }

    fun startCapture() {
        ScreenRecordService.screenRecordService!!.startCapture()
    }

    fun stopCapture() {
        val service = Intent(activity, ScreenRecordService::class.java)
        activity.stopService(service)
    }

}