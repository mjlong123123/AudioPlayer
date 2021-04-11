package com.dragon.audioplayer.codec

import android.media.*
import android.media.AudioTrack.MODE_STREAM
import android.media.AudioTrack.WRITE_BLOCKING
import android.util.Log

abstract class AudioDecodeCodec(mediaFormat: MediaFormat) : BufferDecodeCodec(mediaFormat) {
    var audioTrack: AudioTrack? = null;

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        Log.d("audio_dragon","onOutputBufferAvailable $index")
        val buffer = codec.getOutputBuffer(index) ?: return;
        buffer.position(info.offset); 
        audioTrack?.write(buffer, info.size, WRITE_BLOCKING);
        codec.releaseOutputBuffer(index, false);
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        Log.d("audio_dragon","onOutputFormatChanged sampleRate $sampleRate")
        Log.d("audio_dragon","onOutputFormatChanged channelCount $channelCount")
        Log.d("audio_dragon","onOutputFormatChanged minBufferSize $minBufferSize")
        audioTrack = AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelCount, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, MODE_STREAM);
        audioTrack?.play();
    }

    override fun releaseInternal() {
        super.releaseInternal()
        audioTrack?.stop();
        audioTrack?.release();
    }
}