cd `dirname $0`
cd ./src/main/java/
javac ./com/zxzx74147/mediacore/components/audio/mixer/AudioNdkInterface.java
pwd
javah com.zxzx74147.mediacore.components.audio.mixer.AudioNdkInterface
rm ./com/zxzx74147/mediacore/components/audio/mixer/AudioNdkInterface.class

mv ./com_zxzx74147_mediacore_components_audio_mixer_AudioNdkInterface.h ../../../src/main/cpp/audio_interface_gen.cpp