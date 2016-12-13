# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             audio_interface

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/audio_interface.cpp
             src/main/cpp/pcmprocess/audio_process.cpp
             src/main/cpp/pcmprocess/pcmprocess.cpp
             src/main/cpp/pcmprocess/membuffer.cpp)

include_directories(src/main/cpp/libspeex/)
include_directories( src/main/cpp/pcmprocess/ )



add_library( speex-lib
             STATIC
             IMPORTED )

set_target_properties( # Specifies the target library.
             speex-lib
             PROPERTIES IMPORTED_LOCATION
             ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/libspeex/lib/armeabi-v7a/libspeex_static.a )


target_link_libraries(
                       audio_interface
                       # Links to speex
                       speex-lib
                       OpenSLES
                       log
                       )
