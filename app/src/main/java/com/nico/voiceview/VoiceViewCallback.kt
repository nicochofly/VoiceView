package com.nico.voiceview

interface VoiceViewCallback {
    fun cancelAction()
    fun sendAction(message: String)
}