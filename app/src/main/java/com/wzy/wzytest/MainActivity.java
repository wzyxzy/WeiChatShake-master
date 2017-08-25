package com.wzy.wzytest;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;//传感器
    private Vibrator mVibrator;//手机震动
    private TextView notification_text;
    private Button button_speak;
    private LinearLayout activity_main;
    private SpeechSynthesizer mTts;
    private SimpleDateFormat df = new SimpleDateFormat("HH点mm分");
    private TextToSpeech mSpeech;
    private boolean tts_true;
    private SoundPool soundPool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        initView();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void initView() {
        notification_text = (TextView) findViewById(R.id.notification_text);
        notification_text.setOnClickListener(this);
        button_speak = (Button) findViewById(R.id.button_speak);
        button_speak.setOnClickListener(this);
        activity_main = (LinearLayout) findViewById(R.id.activity_main);
        activity_main.setOnClickListener(this);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=599fc82b");
        //1.创建 SpeechSynthesizer 对象, 第二个参数：本地合成时传 InitListener
        mTts = SpeechSynthesizer.createSynthesizer(this, null);
        //2.合成参数设置
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan"); //设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "80");//设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "50");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
//设置合成音频保存位置（可自定义保存位置） ，保存在“./sdcard/iflytek.pcm”
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
        // 创建TTS对象
        mSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    mSpeech.setLanguage(Locale.CHINESE);
                    tts_true = true;
                } else {
                    tts_true = false;
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取 SensorManager 负责管理传感器
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        if (mSensorManager != null) {
            //获取加速度传感器
            Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mAccelerometerSensor != null) {
                mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];
            int minValue = 25;
            if ((Math.abs(x) > minValue || Math.abs(y) > minValue || Math
                    .abs(z) > minValue)) {
                // TODO: 2016/10/19 实现摇动逻辑, 摇动后进行震动
                mVibrator.vibrate(300);
                handler.sendEmptyMessage(0);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_speak:
                handler.sendEmptyMessage(0);
                break;
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
                    int mWeiChatAudio = soundPool.load(getApplicationContext(), R.raw.anoti, 1);
                    soundPool.play(mWeiChatAudio, 1, 1, 0, 0, 1);
                    if (tts_true) {
                        mSpeech.speak("和佳软件提示您,您已经打卡成功，打卡时间为：" + df.format(new Date()), TextToSpeech.QUEUE_FLUSH, null);
                    } else {
                        mTts.startSpeaking("和佳软件提示您,您已经打卡成功，打卡时间为：" + df.format(new Date()), null);
                    }
                    break;
            }
        }
    };
}
