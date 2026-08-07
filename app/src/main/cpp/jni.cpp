#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "whisper.h"

// --- model load straight from the asset stream (no mmap, works whether or not AAPT compresses it) ---
static size_t asset_read(void *ctx, void *out, size_t n) { return AAsset_read((AAsset *) ctx, out, n); }
static bool asset_is_eof(void *ctx) { return AAsset_getRemainingLength64((AAsset *) ctx) <= 0; }
static void asset_close(void *ctx) { AAsset_close((AAsset *) ctx); }

extern "C" JNIEXPORT jlong JNICALL
Java_com_voiclog_data_transcription_WhisperLib_initContextFromAsset(
        JNIEnv *env, jobject, jobject assetManager, jstring assetPathStr) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    const char *path = env->GetStringUTFChars(assetPathStr, nullptr);
    AAsset *asset = AAssetManager_open(mgr, path, AASSET_MODE_STREAMING);
    env->ReleaseStringUTFChars(assetPathStr, path);
    if (!asset) return 0;

    whisper_model_loader loader = { asset, asset_read, asset_is_eof, asset_close };
    return (jlong) whisper_init_with_params(&loader, whisper_context_default_params());
}

extern "C" JNIEXPORT void JNICALL
Java_com_voiclog_data_transcription_WhisperLib_freeContext(JNIEnv *, jobject, jlong ctxPtr) {
    whisper_free((whisper_context *) ctxPtr);
}

// --- progress callback: fires synchronously inside whisper_full, on the same thread and same
// JNI call frame that entered fullTranscribe below — so `env` and the local `listener` ref are
// still valid here with no global-ref or AttachCurrentThread dance needed. ---
struct ProgressCtx { JNIEnv *env; jobject listener; jmethodID onProgress; };

static void progress_trampoline(whisper_context *, whisper_state *, int progress, void *user_data) {
    auto *p = (ProgressCtx *) user_data;
    if (p->listener) p->env->CallVoidMethod(p->listener, p->onProgress, (jint) progress);
}

extern "C" JNIEXPORT void JNICALL
Java_com_voiclog_data_transcription_WhisperLib_fullTranscribe(
        JNIEnv *env, jobject, jlong ctxPtr, jint numThreads, jfloatArray audioData, jobject listener) {
    auto *context = (whisper_context *) ctxPtr;
    jfloat *samples = env->GetFloatArrayElements(audioData, nullptr);
    jsize count = env->GetArrayLength(audioData);

    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_special = false;
    params.translate = false;
    params.language = "en";
    params.n_threads = numThreads;
    params.no_context = true;

    ProgressCtx pctx{env, listener, nullptr};
    if (listener) {
        pctx.onProgress = env->GetMethodID(env->GetObjectClass(listener), "onProgress", "(I)V");
        params.progress_callback = progress_trampoline;
        params.progress_callback_user_data = &pctx;
    }

    whisper_full(context, params, samples, count);
    env->ReleaseFloatArrayElements(audioData, samples, JNI_ABORT);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_voiclog_data_transcription_WhisperLib_getTextSegmentCount(JNIEnv *, jobject, jlong ctxPtr) {
    return whisper_full_n_segments((whisper_context *) ctxPtr);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_voiclog_data_transcription_WhisperLib_getTextSegment(JNIEnv *env, jobject, jlong ctxPtr, jint index) {
    return env->NewStringUTF(whisper_full_get_segment_text((whisper_context *) ctxPtr, index));
}