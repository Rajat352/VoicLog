package com.voiclog.data.audio

fun shortsToNormalizedFloats(samples: ShortArray, count: Int): FloatArray =
    FloatArray(count) { (samples[it] / 32767f).coerceIn(-1f, 1f) }

fun mergeAudioDataChunks(chunks: List<FloatArray>): FloatArray {
    val merged = FloatArray(chunks.sumOf { it.size })
    var offset = 0
    for (chunk in chunks) {
        chunk.copyInto(merged, offset)
        offset += chunk.size
    }
    return merged
}