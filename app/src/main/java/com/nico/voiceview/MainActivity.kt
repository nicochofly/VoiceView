package com.nico.voiceview

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

class MainActivity : Activity(), VoiceOprCallback, VoiceViewCallback {
    lateinit var voiceArcView: VoiceArcView


    var isPress = false


    private lateinit var btn: TextView
    private var handler = Handler()


    private var mCurrentRecString = StringBuilder()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.voice_layout)

        btn = findViewById(R.id.btn)

        voiceArcView = findViewById(R.id.voicearcview)
//        voiceArcView.setCallback(this)

        voiceArcView.setOprCallback(this)
        voiceArcView.setViewCallback(this)

        btn.setOnClickListener {
            if (voiceArcView.visibility == View.GONE) {
                voiceArcView.visibility = View.VISIBLE
            }
        }


        btn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    if (voiceArcView.visibility == View.GONE) {
                        voiceArcView.visibility = View.VISIBLE
                        voiceArcView.responseTouchEvent(event)
                    } else {
                    }
                }
                MotionEvent.ACTION_MOVE -> {

                    if (voiceArcView.visibility == View.VISIBLE) {
                        voiceArcView.responseTouchEvent(event)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (voiceArcView.visibility == View.VISIBLE) {
                        voiceArcView.responseTouchEvent(event)
                    }
                }
            }
            return@setOnTouchListener true
        }
//        btn.setOnTouchListener { v, event ->
//            {
//                var act = event.action
//                when(act)
//                {
//                    MotionEvent.ACTION_DOWN ->""
//                    MotionEvent.ACTION_UP ->""
//                    MotionEvent.ACTION_MOVE -> ""
//                }
//            }
//        }
    }

//    override fun onStatusChanged(isPressed: Boolean) {
//        isPress = isPressed
//        if (isPress) {
//            doRunnable()
//        }
//    }
//
//    override fun cancelHandler() {
//        handler.removeCallbacksAndMessages(null)
//    }

    fun doRunnable() {
        var runnable: Runnable = object : Runnable {
            override fun run() {
                if (isPress) {
                    val result = (0..30).random()
                    voiceArcView.newRefreshElement(result)
                    handler.postDelayed(this, 300)
                }
            }
        }
        handler.postDelayed(runnable, 300)
    }

//    override fun onTouchDown() {
//        handler.post(Runnable {
//            voiceArcView.visibility = View.VISIBLE
//            voiceArcView.postInvalidate()
//        })
//
//    }
//
//    override fun onTouchUp() {
//        handler.post(Runnable { voiceArcView.visibility = View.GONE })
//    }

    override fun startRecord() {
        Log.e(javaClass.simpleName, "startRecord")
    }

    override fun stopRecord() {
        Log.e(javaClass.simpleName, "stopRecord")
    }

    override fun cancelRecord() {
        Log.e(javaClass.simpleName, "cancelRecord")
    }

    override fun cancelAction() {
        Log.e(javaClass.simpleName, "cancelAction")
        resetVoiceView()
        btn.visibility = View.VISIBLE
    }

    override fun sendAction(message: String) {
        Log.e(javaClass.simpleName, "sendAction")
        resetVoiceView()
        btn.visibility = View.VISIBLE
    }

    private fun resetVoiceView() {
        mCurrentRecString.clear()
        voiceArcView.doReset()
        voiceArcView.visibility = View.GONE
    }
}
