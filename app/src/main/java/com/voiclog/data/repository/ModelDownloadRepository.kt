package com.voiclog.data.repository

import android.content.Context
import java.io.File

interface ModelDownloadRepository {
    fun modelFile(): File
}

class ModelDownloadRepositoryImpl (
    private val context: Context
): ModelDownloadRepository {

    override fun modelFile(): File {
        val file = File(context.getExternalFilesDir(null), "gemma3-1b-it-int4.litertlm")

        return file
    }
}