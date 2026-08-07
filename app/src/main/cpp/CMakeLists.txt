cmake_minimum_required(VERSION 3.10)
project(voiclog_whisper)

set(CMAKE_CXX_STANDARD 17)
set(WHISPER_LIB_DIR ${CMAKE_SOURCE_DIR}/whisper)

add_library(voiclog_whisper SHARED
        ${WHISPER_LIB_DIR}/src/whisper.cpp
        ${CMAKE_SOURCE_DIR}/jni.cpp
)

target_compile_definitions(voiclog_whisper PUBLIC GGML_USE_CPU)
target_compile_definitions(voiclog_whisper PRIVATE WHISPER_VERSION="1.9.2")
if(${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(voiclog_whisper PRIVATE -march=armv8.2-a+fp16)
endif()
if(NOT ${CMAKE_BUILD_TYPE} STREQUAL "Debug")
    target_compile_options(voiclog_whisper PRIVATE -O3)
    target_link_options(voiclog_whisper PRIVATE -Wl,--gc-sections -flto)
endif()

find_library(LOG_LIB log)
add_subdirectory(${WHISPER_LIB_DIR}/ggml ${CMAKE_BINARY_DIR}/ggml)
target_link_libraries(voiclog_whisper ${LOG_LIB} android ggml)

include_directories(${WHISPER_LIB_DIR} ${WHISPER_LIB_DIR}/include ${WHISPER_LIB_DIR}/ggml/include)