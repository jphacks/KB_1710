package com.example.onolab_mcbookair1.test2;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class MainActivity extends Activity {

    // サンプリングレート
    int SAMPLING_RATE = 44100;
    // FFTのポイント数
    int FFT_SIZE = 4096;

    // デシベルベースラインの設定
    double dB_baseline = Math.pow(2, 15) * FFT_SIZE * Math.sqrt(2);

    // 分解能の計算
    double resol = ((SAMPLING_RATE / (double) FFT_SIZE));

    AudioRecord audioRec = null;
    boolean bIsRecording = false;
    int bufSize;
    Thread fft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bufSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // AudioRecordの作成
        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufSize * 2);
        audioRec.startRecording();
        bIsRecording = true;

        //フーリエ解析スレッド
        fft = new Thread(new Runnable() {
            @Override
            public void run() {
                byte buf[] = new byte[bufSize * 2];
                while (bIsRecording) {
                    audioRec.read(buf, 0, buf.length);

                    //エンディアン変換
                    ByteBuffer bf = ByteBuffer.wrap(buf);
                    bf.order(ByteOrder.LITTLE_ENDIAN);
                    short[] s = new short[(int) bufSize];
                    for (int i = bf.position(); i < bf.capacity() / 2; i++) {
                        s[i] = bf.getShort();
                    }

                    //FFTクラスの作成と値の引き渡し
                    FFT4g fft = new FFT4g(FFT_SIZE);
                    double[] FFTdata = new double[FFT_SIZE];
                    for (int i = 0; i < FFT_SIZE; i++) {
                        FFTdata[i] = (double) s[i];
                    }
                    fft.rdft(1, FFTdata);

                    // デシベルの計算
                    double[] dbfs = new double[FFT_SIZE / 2];
                    double max_db = -120d;
                    int max_i = 0;
                    for (int i = 0; i < FFT_SIZE; i += 2) {
                        dbfs[i / 2] = (int) (20 * Math.log10(Math.sqrt(Math
                                .pow(FFTdata[i], 2)
                                + Math.pow(FFTdata[i + 1], 2)) / dB_baseline));
                        if (max_db < dbfs[i / 2]) {
                            max_db = dbfs[i / 2];
                            max_i = i / 2;
                        }
                    }

                    //音量が最大の周波数と，その音量を表示
                    Log.d("fft","周波数："+ resol * max_i+" [Hz] 音量：" +  max_db+" [dB]");
                }
                // 録音停止
                audioRec.stop();
                audioRec.release();
            }
        });
        //スレッドのスタート
        fft.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

}