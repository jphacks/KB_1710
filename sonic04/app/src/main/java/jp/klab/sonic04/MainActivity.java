package jp.klab.sonic04;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.jtransforms.fft.DoubleFFT_1D;


public class MainActivity extends AppCompatActivity
        implements Runnable, View.OnClickListener, Handler.Callback {
    private static final String TAG = "SNC";

    private static final int SAMPLE_RATE = 44100;
    private static final short THRESHOLD_AMP = 0x00ff;

    private static final int MSG_RECORD_START = 100;
    private static final int MSG_RECORD_END   = 110;
    private static final int MSG_FREQ_PEAK  = 120;
    private static final int MSG_SILENCE    = 130;

    private Handler mHandler;
    private AudioRecord mAudioRecord = null;

    private Button mButton01;
    private TextView mTextView02;

    private boolean mInRecording = false;
    private boolean mStop = false;
    private int mBufferSizeInShort;

    private short mRecordBuf[];
    private DoubleFFT_1D mFFT;
    private double mFFTBuffer[];
    private int mFFTSize;
    private timer time;
    private TextView mtextView;
    private long startTime;
    private long stopTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mHandler = new Handler(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//^-------------------------------------------------------------------------------


        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();


        Button deleteAllButton = (Button) findViewById(R.id.deleteAll);
        deleteAllButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //String name = nameText.getText().toString();
                //String age = ageText.getText().toString();
                db.delete("data", null, null);
                Log.d(TAG, "DELETE");

            }
        });
        //登録
        Button entryButton = (Button) findViewById(R.id.insert);
        entryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues insertValues = new ContentValues();
                insertValues.put("F", doFFT(mRecordBuf));
                Log.d(TAG, "SAVE");
                //insertValues.put("age", age);
            }
        });




        Button detaBaseButton = (Button) findViewById(R.id.dataBase);
        detaBaseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dbIntent = new Intent(MainActivity.this,
                        ShowDataBase.class);
                startActivity(dbIntent);
            }
        });





//------------------------------------------------------------------------------------------

        mButton01 = (Button) findViewById(R.id.button01);
        mButton01.setOnClickListener(this);
        mTextView02 = (TextView) findViewById(R.id.textView02);

        int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        mBufferSizeInShort = bufferSizeInBytes / 2;
        // 録音用バッファ
        mRecordBuf = new short[mBufferSizeInShort];

        // FFT 処理用
        mFFTSize = mBufferSizeInShort;
        mFFT = new DoubleFFT_1D(mFFTSize);
        mFFTBuffer = new double[mFFTSize];

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes);

    }



    @Override
    public void onClick(View v) {
        if (v == (View)mButton01) {
            // 集音開始 or 終了
            if (!mInRecording) {
                mInRecording = true;
                new Thread(this).start();
            } else {
                mInRecording = false;
            }
        }
        return;
    }



    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RECORD_START:
                Log.d(TAG, "MSG_RECORD_START");
                mButton01.setText("STOP");

                break;
            case MSG_RECORD_END:
                Log.d(TAG, "MSG_RECORD_END");
                mButton01.setText("START");

                break;
            case MSG_FREQ_PEAK:
                mTextView02.setText(Integer.toString(msg.arg1) + " Hz");

               /* // データの保存
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt("text_view",msg.arg1);
                editor.commit();
*/
                break;
            case MSG_SILENCE:
                mTextView02.setText("");
                break;
        }

       // int data = preferences.getInt("text_view" , 1);
       //  ((TextView)findViewById(data)).setText("" + text_view);
        return true;
    }

    @Override
    public void run() {
        boolean bSilence = false;
        mHandler.sendEmptyMessage(MSG_RECORD_START);
        // 集音開始
        mAudioRecord.startRecording();
        while (mInRecording && !mStop) {
            mAudioRecord.read(mRecordBuf, 0, mBufferSizeInShort);
            bSilence = true;
            for (int i = 0; i < mBufferSizeInShort; i++) {
                short s = mRecordBuf[i];
                if (s > THRESHOLD_AMP) {
                    bSilence = false;
                }
            }
            if (bSilence) { // 静寂
                mHandler.sendEmptyMessage(MSG_SILENCE);
                continue;
            }
            int freq = doFFT(mRecordBuf);
            Message msg = new Message();
            msg.what = MSG_FREQ_PEAK;
            msg.arg1 = freq;
            mHandler.sendMessage(msg);

            int result[] = {doFFT(mRecordBuf)};

            for (int i = 0; i <100; i++){

            }

        }
        // 集音終了
        mAudioRecord.stop();
        mHandler.sendEmptyMessage(MSG_RECORD_END);
    }

    private int doFFT(short[] data) {
        for (int i = 0; i < mFFTSize; i++) {
            mFFTBuffer[i] = (double)data[i];
        }
        // FFT 実行
        mFFT.realForward(mFFTBuffer);

        // 処理結果の複素数配列から各周波数成分の振幅値を求めピーク分の要素番号を得る
        double maxAmp = 0;
        int index = 0;
        for (int i = 0; i < mFFTSize/2; i++) {
            double a = mFFTBuffer[i*2]; // 実部
            double b = mFFTBuffer[i*2 + 1]; // 虚部
            // a+ib の絶対値 √ a^2 + b^2 = r が振幅値
            double r = Math.sqrt(a*a + b*b);
            if (r > maxAmp) {
                maxAmp = r;
                index = i;
            }
        }
        // 要素番号・サンプリングレート・FFT サイズからピーク周波数を求める
        return index * SAMPLE_RATE / mFFTSize;
    }



}
