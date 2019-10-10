package com.byl.voicetest.util;

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 语音合成工具类
 * 限制字节数 8192，汉字个数为4096个
 * Created by baiyuliang on 2016-5-17.
 */
public class SpeechSynthesizerUtil {

    Activity context;
    private SpeechSynthesizer mTts;

    public SpeechSynthesizerUtil(Activity context) {
        this.context = context;
        this.mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
        setParamIts();
    }

    public void startSpeech(String content, SynthesizerListener synthesizerListener) {
        if (!TextUtils.isEmpty(content)) {
            mTts.startSpeaking(content, synthesizerListener);
        }
    }

    public void pauseSpeaking() {
        if (mTts != null)
            mTts.pauseSpeaking();
    }

    public void stopSpeech() {
        if (mTts != null)
            mTts.stopSpeaking();
    }

    public void resumeSpeech() {
        if (mTts != null)
            mTts.resumeSpeaking();
    }

    public void destroy() {
        if (mTts != null)
            mTts.stopSpeaking();
        mTts.destroy();
    }

    /**
     * 语音合成参数设置
     *
     * @return
     */
    private void setParamIts() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数(默认 云)
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "100");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/tts.wav");
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
    }

    /**
     * 语音合成初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {

            } else {

            }
        }
    };


}
