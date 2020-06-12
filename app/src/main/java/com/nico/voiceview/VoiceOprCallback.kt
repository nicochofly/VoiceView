package com.nico.voiceview

interface VoiceOprCallback {
    fun startRecord()
    fun stopRecord()
    fun cancelRecord()
}