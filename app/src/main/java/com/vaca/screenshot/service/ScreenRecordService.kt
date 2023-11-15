package com.vaca.screenshot.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import com.vaca.screenshot.MainActivity
import com.vaca.screenshot.PathUtil
import com.vaca.screenshot.R
import com.vaca.screenshot.utils.ScreenUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ScreenRecordService : Service() {
    private val TAG = "ScreenRecordService"
    private var mImageReader: ImageReader? = null

    /**
     * 是否为标清视频
     */
    private val isVideoSd = false
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mScreenDensity = 0
    private var mResultCode = 0
    private var mResultData: Intent? = null
    private var mMediaProjection: MediaProjection? = null
    private val mMediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        return null
    }

    private var mFloatLayout: LinearLayout? = null
    private var wmParams: WindowManager.LayoutParams? = null
    private var mWindowManager: WindowManager? = null
    private var inflater: LayoutInflater? = null
    private var mFloatView: ImageButton? = null
    private fun createFloatView() {
        wmParams = WindowManager.LayoutParams()
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        wmParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        wmParams!!.format = PixelFormat.RGBA_8888
        wmParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        wmParams!!.gravity = Gravity.LEFT or Gravity.TOP
        wmParams!!.x = 0
        wmParams!!.y = 0
        wmParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFloatLayout = inflater!!.inflate(R.layout.float_layout, null) as LinearLayout
        mWindowManager!!.addView(mFloatLayout, wmParams)
        mFloatView = mFloatLayout!!.findViewById<View>(R.id.float_id) as ImageButton
        mFloatLayout!!.measure(
            View.MeasureSpec.makeMeasureSpec(
                0,
                View.MeasureSpec.UNSPECIFIED
            ), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        mFloatView!!.setOnTouchListener { v, event -> // TODO Auto-generated method stub
            wmParams!!.x = event.rawX.toInt() - mFloatView!!.measuredWidth / 2
            wmParams!!.y = event.rawY.toInt() - mFloatView!!.measuredHeight / 2 - 25
            mWindowManager!!.updateViewLayout(mFloatLayout, wmParams)
            false
        }
        mFloatView!!.setOnClickListener {
            // hide the button
            mFloatView!!.visibility = View.INVISIBLE
            val handler2 = Handler()
            handler2.postDelayed({ //capture the screen
                startCapture()
            }, 500)
            val handler3 = Handler()
            handler3.postDelayed({
                mFloatView!!.visibility = View.VISIBLE
                //stopVirtual();
            }, 1000)
        }
        Log.i(TAG, "created the float sphere view")
    }

    override fun onCreate() {
        screenRecordService = this
        createFloatView()
        super.onCreate()
    }

    private fun createNotificationChannel() {
        val builder = Notification.Builder(this.applicationContext) //获取一个Notification构造器
        val nfIntent = Intent(this, MainActivity::class.java) //点击后跳转的界面，可以设置跳转数据
        builder.setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                nfIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        ) // 设置PendingIntent
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            ) // 设置下拉列表中的图标(大图标)
            //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setContentText("is running......") // 设置上下文内容
            .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id")
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "notification_id",
                "notification_name",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = builder.build() // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND //设置为默认的声音
        startForeground(110, notification)
    }

    fun startCapture() {
        val image = mImageReader!!.acquireLatestImage()
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap =
            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap!!.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        image.close()
        Log.i(TAG, "image data captured")
        if (bitmap != null) {
            try {
                val fileImage = File(PathUtil.getPathX("xx.jpg"))
                if (!fileImage.exists()) {
                    fileImage.createNewFile()
                    Log.i(TAG, "image file created")
                }
                val out = FileOutputStream(fileImage)
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()
                    val media = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(fileImage)
                    media.data = contentUri
                    this.sendBroadcast(media)
                    Log.i(TAG, "screen image saved")
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mResultCode = intent.getIntExtra("resultCode", 1)
        mResultData = intent.getParcelableExtra("data")
        createNotificationChannel()
        screenBaseInfo
        mMediaProjection = createMediaProjection()
        mImageReader =
            ImageReader.newInstance(mScreenWidth, mScreenHeight,  android.graphics.PixelFormat.RGBA_8888, 2)
        mVirtualDisplay =
            createVirtualDisplay() // 必须在mediaRecorder.prepare() 之后调用，否则报错"fail to get surface"
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null)
            mMediaProjection!!.stop()
            mMediaRecorder.reset()
        }
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    /**
     * 获取屏幕相关数据
     */
    private val screenBaseInfo: Unit
        /**
         * 获取屏幕相关数据
         */
        private get() {
            mScreenWidth = ScreenUtils.getScreenWidth(this)
            mScreenHeight = ScreenUtils.getFullActivityHeight(this)
            Log.e("vaca", "$mScreenWidth $mScreenHeight")
            mScreenDensity = ScreenUtils.getScreenDensityDpi(this)
        }

    private fun createMediaProjection(): MediaProjection {
        Log.i(TAG, "Create MediaProjection")
        return (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(
            mResultCode,
            mResultData!!
        )
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        Log.i(TAG, "Create VirtualDisplay")
        return mMediaProjection!!.createVirtualDisplay(
            TAG, mScreenWidth, mScreenHeight, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader!!.surface, null, null
        )
    }

    companion object {
        @JvmField
        var screenRecordService: ScreenRecordService? = null
    }
}