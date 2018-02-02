package com.zxing.activity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.R;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class CaptureActivity extends AppCompatActivity implements Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private RelativeLayout rl_back;
    private TextView cancelScanButton;
    /**
     * 用户判断和加载不同的界面
     */
    private String payType = null;
    private TextView promptTv;
    //是否开灯
    private boolean isOn = false;

    /**
     * Called when the activity is first created. sdada
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
//        payType = intent.getStringExtra("from");//获取支付方式  alipay支付宝，weixin微信
//        if (payType != null && ("alipay".equals(payType) || "weixin".equals(payType))) {
//            setContentView(R.layout.payfor_camera);
//            viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
//            topBarLayout = (MkTopBarLayout) findViewById(R.id.topbar);
//            topBarLayout.setTitle("二维码／条码");
//            topBarLayout.setRightViewText("开灯");
//            topBarLayout.setRigthViewTypeMode(RightViewTypeMode.TEXT);
//            topBarLayout.setLeftTxvOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CaptureActivity.this.finish();
//                }
//            });
//            topBarLayout.setRightTxvOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (CameraManager.get().getCamera() != null) {
//                        if (!isOn) {
//                            isOn = true;
//                            topBarLayout.setRightViewText("关灯");
//                            Parameters parameters = CameraManager.get().getCamera().getParameters();
//                            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);//开启
//                            CameraManager.get().getCamera().setParameters(parameters);
//
//                        } else {
//                            isOn = false;
//                            topBarLayout.setRightViewText("开灯");
//                            Parameters parameters = CameraManager.get().getCamera().getParameters();
//                            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);//开启
//                            CameraManager.get().getCamera().setParameters(parameters);
//                        }
//                    }
//                }
//            });
//            promptTv = (TextView) findViewById(R.id.tv_prompt);
//            String str = "";
//            if ("alipay".equals(payType)) { //alipay支付宝
//                str = "请顾客按一下操作：\n打开支付宝——>付款——>付款码";
//            } else if ("weixin".equals(payType)) {//weixin微信
//                str = "请顾客按一下操作：\n打开微信——>我——>钱包——>付款——>付款码";
//            }
//            promptTv.setText(str);
//        } else {
        setContentView(R.layout.camera);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        //quit the scan view
        rl_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });

        //     }
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
//		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
//		cancelScanButton = (Button)findViewById(R.id.button_back);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
//		Rect rect= CameraManager.get().getFramingRect();
//		rect.set(100, 300, 200, 400);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        if (isOn == true) {
            if (CameraManager.get().getCamera() != null) {
                Parameters parameters = CameraManager.get().getCamera().getParameters();
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);//开启
                CameraManager.get().getCamera().setParameters(parameters);
            }
        }
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        String resultString = result.getText();
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        System.out.println("-----------------Result:" + resultString);
        if (resultString.equals("")) {
            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("scan_result", resultString);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
            CaptureActivity.this.finish();
        }

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);

        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}
