package com.sven.pusher.live.channel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.sven.pusher.live.LivePusher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioChannel {

    private int channels = 1;//声道数
    private ExecutorService executor;
    private boolean isLiving;
    private AudioRecord audioRecord;//录音机
    private LivePusher mLivePusher;
    private int inputSamples;

    public AudioChannel(LivePusher livePusher){
        mLivePusher = livePusher;
        //准备录音机，手机采集pcm数据，然后将pcm数据编码成faac格式以流媒体形式发送到服务器，服务器再解码播放出来

        //创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
        executor = Executors.newSingleThreadExecutor();
        int channelConfig;
        if(channels == 2){
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        }else {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }

        //初始化录音机
        mLivePusher.native_setAudioEncInfo(44100, channels);
        //16位 2个字节
        inputSamples = mLivePusher.getInputSamples() * 2;

        //最小需要的缓冲区
        int minBufferSize = AudioRecord.getMinBufferSize(44100, channelConfig, AudioFormat.ENCODING_PCM_16BIT) * 2;
        //1、麦克风 2、采样率 3、声道数 4、采样位
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,44100,channelConfig,AudioFormat.ENCODING_PCM_16BIT,minBufferSize > inputSamples ? minBufferSize:inputSamples);
    }


    public void stopLive() {
        isLiving = false;
    }

    public void startLive() {
        isLiving = true;
        executor.submit(new AudioTeask());
    }

    public void release() {
        audioRecord.release();
    }

    class AudioTeask implements Runnable{

        @Override
        public void run() {
            //启动录音机
            audioRecord.startRecording();
            byte[] bytes = new byte[inputSamples];
            while(isLiving){
                int len = audioRecord.read(bytes,0,bytes.length);
                if(len > 0 ){
                    //将读到的pcm数据送去编码
                    mLivePusher.native_pushAudio(bytes);
                }
            }
            audioRecord.stop();
        }
    }


}
