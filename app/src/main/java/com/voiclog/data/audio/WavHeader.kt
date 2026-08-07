package com.voiclog.data.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun buildWavHeader(pcmDataSize: Long, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
    val byteRate = sampleRate * channels * bitsPerSample / 8
    val blockAlign = channels * bitsPerSample / 8
    val totalDataLen = pcmDataSize + 36

    return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
        put("RIFF".toByteArray())
        putInt(totalDataLen.toInt())
        put("WAVE".toByteArray())
        put("fmt ".toByteArray())
        putInt(16) // PCM sub-chunk size
        putShort(1) // audio format = PCM
        putShort(channels.toShort())
        putInt(sampleRate)
        putInt(byteRate)
        putShort(blockAlign.toShort())
        putShort(bitsPerSample.toShort())
        put("data".toByteArray())
        putInt(pcmDataSize.toInt())
    }.array()
}