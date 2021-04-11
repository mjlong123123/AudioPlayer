package com.dragon.audioplayer.codec

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat

/**
 * @author dragon
 */
abstract class BufferDecodeCodec(mediaFormat: MediaFormat) : BaseCodec("", mediaFormat) {
    override fun onCreateMediaCodec(mediaFormat: MediaFormat): MediaCodec {
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val mediaCodecName = mediaCodecList.findDecoderForFormat(mediaFormat)
        return MediaCodec.createByCodecName(mediaCodecName)
    }

    override fun onConfigMediaCodec(mediaCodec: MediaCodec) {
        mediaCodec.configure(mediaFormat, null, null, 0)
    }
}