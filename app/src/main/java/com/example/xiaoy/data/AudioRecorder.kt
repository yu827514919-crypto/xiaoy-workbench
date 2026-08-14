package com.example.xiaoy.data

import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

/**
 * 录音封装：MediaRecorder 录成 AAC（.m4a），存到指定文件。
 * 用法：start(file) -> 录音中 -> stop() 返回文件；取消则 cancel()。
 */
class AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    val isRecording: Boolean get() = recorder != null

    /** 开始录音，失败返回 null */
    fun start(target: File): File? = try {
        stop()
        file = target
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(target.absolutePath)
            prepare()
            start()
        }
        target
    } catch (_: Exception) {
        recorder?.release()
        recorder = null
        file = null
        null
    }

    /** 停止并保存，返回录音文件；失败返回 null */
    fun stop(): File? = try {
        recorder?.apply { stop(); release() }
        recorder = null
        file
    } catch (_: Exception) {
        recorder?.release()
        recorder = null
        val f = file
        file = null
        f
    }

    /** 取消录音并删除临时文件 */
    fun cancel() {
        try { recorder?.stop() } catch (_: Exception) { }
        recorder?.release()
        recorder = null
        file?.delete()
        file = null
    }
}

/** 播放封装：简单封装 MediaPlayer，播放 .m4a/.mp3 */
class AudioPlayer {
    private var player: MediaPlayer? = null
    private var playingPath: String? = null

    val isPlaying: Boolean get() = player != null

    /** 播放；若已在播放同一文件则停止（toggle） */
    fun toggle(path: String): Boolean = try {
        if (player != null && playingPath == path) {
            stop()
            return false
        }
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener { stop() }
            prepare()
            start()
        }
        playingPath = path
        true
    } catch (_: Exception) {
        stop()
        false
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) { }
        player?.release()
        player = null
        playingPath = null
    }
}
