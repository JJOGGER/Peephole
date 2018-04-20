package cn.jcyh.peephole.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.base.BaseActivity;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.ImgUtil;
import timber.log.Timber;

public class PictureActivity extends BaseActivity {
    @BindView(R.id.surface_picture)
    SurfaceView mSurfaceView;
    private Camera mCamera;

    @Override
    public int getLayoutId() {
        return R.layout.activity_picture;
    }

    @Override
    protected void init() {
        mSurfaceView.getHolder().addCallback(mCallback);
    }

    private void initCamera()

    {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open();
//        Camera.Parameters parameters = mCamera.getParameters();
//
//        parameters.setPictureFormat(PixelFormat.JPEG);
//
//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
//
//        setDispaly(parameters, mCamera);
//
//        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());//进行预览
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("-------------<" + e.getMessage());
        }
        mCamera.startPreview();//开始预览，必须在调用相机拍照之前
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                onTakePhoto(data);
            }
        });
    }

    private void onTakePhoto(byte[] data) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        final String tempPath = FileUtil.getInstance().getDoorbellMediaPath() + File.separator + "IMG_" + time +
                ".jpg";
        boolean isCompleted = ImgUtil.createWaterMaskRightBottom2File(this,
                tempPath, bitmap,
                BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                10, 10);
        closeCamera();

        Intent intent = new Intent();
        intent.putExtra("filePath", tempPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void closeCamera() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.cancelAutoFocus();
        }catch (Exception e){
            e.printStackTrace();
            Timber.e("---->"+e.getMessage());
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }

    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, i);
            }
        } catch (Exception ignored) {
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Timber.e("----------->a:");
            initCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Timber.e("----------->b:");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Timber.e("----------->c:");
        }
    };
}
