//
// Created by zhengxin on 2016/12/13.
//

#ifndef MEDIACOREDEMO_AUDIO_PROCESS_H
#define MEDIACOREDEMO_AUDIO_PROCESS_H
#include <SLES/OpenSLES.h>
#include <stdlib.h>


class AudioProcess {

private:
    // engine interfaces
     SLObjectItf engineObject = NULL;
     SLEngineItf engineEngine;

    // output mix interfaces
     SLObjectItf outputMixObject = NULL;
     SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
     const SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
public:
    int createEngine();
};


#endif //MEDIACOREDEMO_AUDIO_PROCESS_H
