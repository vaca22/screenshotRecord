package com.vaca.screenshot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vaca.screenshot.service.ScreenRecordService
import com.vaca.screenshot.utils.CaptureUtils
import com.vaca.screenshot.utils.CaptureUtils.createScreenCapture
import com.vaca.screenshot.utils.CaptureUtils.registerCapture
import com.vaca.screenshot.utils.CaptureUtils.startCapture


/**
 * @author by talon, Date on 19/6/23.
 * note:
 */
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PathUtil.initVar(this)
        registerCapture(this)

    }

    fun StartRecorder(view: View?) {
        createScreenCapture()
    }





    fun gaga(view: View?) {
        startCapture()

    }

    fun StopRecorder(view: View) {
        CaptureUtils.stopCapture()
    }


}