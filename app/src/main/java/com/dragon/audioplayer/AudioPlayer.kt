package com.dragon.audioplayer

import android.media.MediaCodec
import android.media.MediaFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dragon.audioplayer.codec.AudioDecodeCodec
import com.dragon.rtplib.RtpWrapper
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class AudioPlayer {
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val audioRtpWrapper = RtpWrapper()
    private lateinit var audioDecodeCodec: AudioDecodeCodec
    private val indexArray = ArrayBlockingQueue<Int>(10)


    private val audioChannelCount = 1;
    private val audioProfile = 1

    /**
     *  97000, 88200, 64000, 48000,44100, 32000, 24000, 22050,16000, 12000, 11025, 8000,7350, 0, 0, 0
     */
    private val audioIndex = 4
    private val audioSpecificConfig = ByteArray(2).apply {
        this[0] = ((audioProfile + 1).shl(3).and(0xff)).or(audioIndex.ushr(1).and(0xff)).toByte()
        this[1] = ((audioIndex.shl(7).and(0xff)).or(audioChannelCount.shl(3).and(0xff))).toByte()
    }.let {
        val buffer = ByteBuffer.allocate(2)
        buffer.put(it)
        buffer.position(0)
        buffer
    }

    init {
        _isPlaying.value = false
    }

    fun start(hasAuHeader: Boolean) {
        if (_isPlaying.value == false) {
            _isPlaying.value = true
            val sampleRate = 44100
            val audioFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                audioChannelCount
            )
            audioFormat.setByteBuffer("csd-0", audioSpecificConfig)
            var currentTime = 0L
            indexArray.clear()
            audioDecodeCodec = object : AudioDecodeCodec(audioFormat) {
                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    indexArray.put(index)
                }
            }

            audioRtpWrapper.open(40020, 97, 1000);
            audioRtpWrapper.setCallback { data, len ->
                if (len < 4) return@setCallback
                val index = indexArray.take()
                if (currentTime == 0L) {
                    currentTime = System.currentTimeMillis()
                }
                val buffer = audioDecodeCodec.mediaCodec.getInputBuffer(index)
                val time = (System.currentTimeMillis() - currentTime) * 1000
                if (hasAuHeader) {
                    buffer?.position(0)
                    buffer?.put(data, 4, len - 4);
                    buffer?.position(0)
                    audioDecodeCodec.mediaCodec.queueInputBuffer(index, 0, len - 4, time, 1)
                } else {
                    buffer?.position(0)
                    buffer?.put(data, 0, len);
                    buffer?.position(0)
                    audioDecodeCodec.mediaCodec.queueInputBuffer(index, 0, len, time, 1)
                }
            };
        }
    }

    fun stop() {
        if (_isPlaying.value == true) {
            _isPlaying.value = false
            audioRtpWrapper.close();
            audioDecodeCodec.release()
        }
    }
}