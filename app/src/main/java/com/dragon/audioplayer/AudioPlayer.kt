package com.dragon.audioplayer

import android.media.MediaCodec
import android.media.MediaFormat
import com.dragon.audioplayer.codec.AudioDecodeCodec
import com.dragon.rtplib.RtpWrapper
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class AudioPlayer {
    private val audioRtpWrapper = RtpWrapper()
    private val audioDecodeCodec: AudioDecodeCodec
    private val audioBufferQueue = ArrayBlockingQueue<ByteArray>(10);
    private val audioBufferSizeQueue = ArrayBlockingQueue<Int>(10);

    init {
        val sampleRate = 44100
        val channelCount = 1
        val audioFormat =
            MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount)
        val temp = ByteBuffer.allocate(2)
        temp.put(0x12).put(0x08)
        audioFormat.setByteBuffer("csd-0", temp)
        temp.position(0)
        var currentTime = 0L
        audioDecodeCodec = object : AudioDecodeCodec(audioFormat) {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (currentTime == 0L) {
                    currentTime = System.currentTimeMillis()
                }
                val data = audioBufferQueue.take();
                val size = audioBufferSizeQueue.take();
                val buffer = codec.getInputBuffer(index);
                val time = (System.currentTimeMillis() - currentTime) * 1000
                buffer?.position(0)
                buffer?.put(data, 4, size - 4);
                buffer?.position(0)
                codec.queueInputBuffer(index, 0, size - 4, time, 1)
            }
        }

        audioRtpWrapper.open(40020, 97, 1000);
        audioRtpWrapper.setCallback { data, len ->
            audioBufferQueue.put(data);
            audioBufferSizeQueue.put(len);
        };
    }

    fun release() {
        audioDecodeCodec.release {
            audioRtpWrapper.close();
        }
    }

}