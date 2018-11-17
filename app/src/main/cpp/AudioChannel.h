//
// Created by Administrator on 2018/11/13 0013.
//

#ifndef PUSHER_AUDIOCHANNEL_H
#define PUSHER_AUDIOCHANNEL_H

#include <sys/types.h>
#include "macro.h"
#include "faac.h"
#include "librtmp/rtmp.h"

class AudioChannel {
    typedef  void (*AudioCallback)(RTMPPacket *packet);

public:
    AudioChannel();

    ~AudioChannel();

    void setAudioEncInfo(int samplesInHZ,int channels);

    void setAudioCallback(AudioCallback audioCallback);

    int getInputSamples();

    void encodeData(int8_t * data);

    RTMPPacket* getAudioTag();

private:
    AudioCallback audioCallback;
    int mChannels;
    u_long inputSamples;
    u_long maxOutputBytes;
    faacEncHandle audioCodec = 0;//编码器
    u_char *buffer = 0;
};


#endif //PUSHER_AUDIOCHANNEL_H
