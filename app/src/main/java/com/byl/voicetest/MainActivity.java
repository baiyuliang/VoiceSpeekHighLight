package com.byl.voicetest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.byl.voicetest.util.SpeechSynthesizerUtil;
import com.byl.voicetest.util.StatusBarUtil;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String TAG = "voicetest";
    Button btn_start, btn_resume, btn_pause;
    TextView tv_progress, tv_content;
    ScrollView mScrollView;
    SeekBar mSeekBar;
    //    TextView tv_duration_progress, tv_duration;
    ProgressBar mProgressBar;

    String content;
    List<SpeechSynthesizerUtil> speechSynthesizerUtils;//朗读工具类
    List<String> contentList;//分段朗读内容
    List<Integer> percentList;//分段内容所占百分比

//    SpeechSynthesizerUtil speechSynthesizerUtil;

    int currentIndex = -1;
    int line = 0;
    int mScrollViewHight;
    float textLineHeight;
    int scrollY = 0;

    int currentPlayingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.darkMode(this);
        StatusBarUtil.immersive(this);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_resume = findViewById(R.id.btn_resume);
        btn_pause = findViewById(R.id.btn_pause);
        tv_progress = findViewById(R.id.tv_progress);
        tv_content = findViewById(R.id.tv_content);
        mScrollView = findViewById(R.id.mScrollView);
        mSeekBar = findViewById(R.id.mSeekBar);
        mProgressBar = findViewById(R.id.mProgressBar);
        mScrollViewHight = getScreenHeight(this) - dp2Px(this, 220);
        content = tv_content.getText().toString();
        Log.e(TAG, "内容长度>>" + content.length());

        tv_content.post(new Runnable() {
            @Override
            public void run() {
                int textHeight = tv_content.getHeight();
                textLineHeight = (float) textHeight / tv_content.getLineCount();
            }
        });

        mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int _scrollY, int oldScrollX, int oldScrollY) {
                scrollY = _scrollY;
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 100) {
                    progress = 99;
                }
                speechSynthesizerUtils.get(currentPlayingPosition).stopSpeech();
                int index = content.length() * progress / 100;
                Log.e(TAG, "seekBar>>" + progress + ",index>>" + index);
                line = 0;
                currentIndex = -1;
                for (int i = 0; i < contentList.size(); i++) {
                    if (index <= (i + 1) * 4000) {
                        currentPlayingPosition = i;
                        if (i == contentList.size() - 1) {
                            speech(content.substring(index), true, progress);
                        } else {
                            speech(content.substring(index, (i + 1) * 4000), true, progress);
                        }
                        break;
                    }
                }

            }
        });

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
    }

    void initData() {
        speechSynthesizerUtils = new ArrayList<>();
        contentList = new ArrayList<>();
        percentList = new ArrayList<>();
        int size;
        if (content.length() > 4000) {
            size = content.length() / 4000 + 1;
        } else {
            size = 1;
        }
        for (int i = 0; i < size; i++) {
            SpeechSynthesizerUtil speechSynthesizerUtil = new SpeechSynthesizerUtil(this);
            speechSynthesizerUtils.add(speechSynthesizerUtil);
            String contentSplit;
            if (i == size - 1) {
                contentSplit = content.substring(i * 4000);
                percentList.add(100);
            } else {
                contentSplit = content.substring(i * 4000, (i + 1) * 4000);
                percentList.add(getPercent((double) content.substring(0, (i + 1) * 4000).length() * 100 / content.length()));
            }
            contentList.add(contentSplit);

        }
    }

    public void initClick() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                Log.e(TAG, "开始");
                scrollY = 0;
                line = 0;
                currentIndex = -1;
                mScrollView.smoothScrollTo(0, 0);
                currentPlayingPosition = 0;
                speech(contentList.get(currentPlayingPosition), false, 0);
            }
        });

        btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "继续");
                speechSynthesizerUtils.get(currentPlayingPosition).resumeSpeech();
            }
        });

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "暂停");
                speechSynthesizerUtils.get(currentPlayingPosition).pauseSpeaking();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initData();
        initClick();
    }

    void speech(final String speechContent, final boolean isSeek, final int progress) {
//        Log.e(TAG, "speechContent>>" + speechContent.substring(0, 20));
        speechSynthesizerUtils.get(currentPlayingPosition).startSpeech(speechContent, new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {
            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {
                if (isSeek) {//拖动seekbar后的进度
                    tv_progress.setText("播放进度:" + (progress + (i + 1) * (percentList.get(currentPlayingPosition) - progress) / 100) + "%");
                    mSeekBar.setProgress(progress + (i + 1) * (percentList.get(currentPlayingPosition) - progress) / 100);
                } else {//正常播放的进度
                    tv_progress.setText("播放进度:" + ((i + 1) * percentList.get(currentPlayingPosition) / 100) + "%");
                    mSeekBar.setProgress(i);
                }
                if (currentIndex != i1) {
                    currentIndex = i1;
                    scrollText(speechContent.substring(i1, i2 + 1).trim());
                    SpannableString spannableString = new SpannableString(content);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#0099EE"));
                    if (isSeek) {
                        int index = content.length() * progress / 100;
                        Log.e(TAG, "progress>>" + progress + ",index>>" + index);
                        spannableString.setSpan(colorSpan, index + i1, index + i2 + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    } else {
                        spannableString.setSpan(colorSpan, currentPlayingPosition * 4000 + i1, currentPlayingPosition * 4000 + i2 + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }

                    tv_content.setText(spannableString);
                }
            }

            @Override
            public void onCompleted(SpeechError speechError) {
                currentPlayingPosition++;
                if (currentPlayingPosition >= speechSynthesizerUtils.size()) {
                    currentPlayingPosition--;
                    return;
                }
                speech(contentList.get(currentPlayingPosition), isSeek, percentList.get(currentPlayingPosition));
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });
    }

    /**
     * 获取正在读的文字所在行数，并判断该行在ui上是否可见，不可见时自动滚动至可见
     * 判断方法：传入正在阅读的内容，遍历整篇内容每行的内容进行比较（是否包含），
     * 举例：如第1行没有包含正在阅读的内容，那么继续叠加判断 前2行是否包含，如仍不包含就继续判断前3行是否包含，
     * 直到确定包含为止。
     *
     * @param current_words
     */
    void scrollText(String current_words) {
        if (TextUtils.isEmpty(current_words)) {
            return;
        }
        Log.i(TAG, "正在读>>" + current_words);
        Layout layout = tv_content.getLayout();
        int end;
        //循环遍历打印每一行
        for (int i = line; i < tv_content.getLineCount(); i++) {
            end = layout.getLineEnd(i);
            String lineText = content.substring(0, end); //指定行的内容
//            Log.e(TAG, "行" + i + ">>" + lineText);
            if (lineText.contains(current_words)) {
                int currentLineTextHeight = (int) (i * textLineHeight - scrollY);
                if (currentLineTextHeight >= 0) {
                    if (currentLineTextHeight > mScrollViewHight) {
                        mScrollView.smoothScrollBy(0, currentLineTextHeight - mScrollViewHight + mScrollViewHight / 2);//滚动到屏幕中央
                    }
                } else {
                    mScrollView.smoothScrollBy(0, currentLineTextHeight - mScrollViewHight / 2);//滚动到屏幕中央
                }
                line = i;//记录正在读的内容所在行（以最后的文字所在行为准），下次遍历时直接从该行开始
                break;
            }
        }
    }

    public int getScreenHeight(Activity activity) {
        int height = 0;
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        height = display.getHeight();
        return height;
    }

    public int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public String formattime(long time) {
        String min = (time / (1000 * 60)) + "";
        String second = (time % (1000 * 60) / 1000) + "";
        if (min.length() < 2) {
            min = 0 + min;
        }
        if (second.length() < 2) {
            second = 0 + second;
        }
        return min + ":" + second;
    }


    public int getPercent(double d) {
        return new BigDecimal(d).setScale(0, BigDecimal.ROUND_UP).intValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < speechSynthesizerUtils.size(); i++) {
            speechSynthesizerUtils.get(i).stopSpeech();
            speechSynthesizerUtils.get(i).destroy();
        }

    }
}
