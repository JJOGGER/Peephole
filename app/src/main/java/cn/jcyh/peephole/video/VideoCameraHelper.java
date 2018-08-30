package cn.jcyh.peephole.video;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.jcyh.peephole.control.ControlCenter;
import cn.jcyh.peephole.entity.DoorbellConfig;
import cn.jcyh.peephole.event.DoorbellSystemAction;
import cn.jcyh.peephole.utils.FileUtil;
import cn.jcyh.peephole.utils.Util;


// Camera包装类，实现本地视频采集
public class VideoCameraHelper implements SurfaceHolder.Callback {
    private final static String TAG = "ANYCHAT";
    private Camera mCamera = null;
    private boolean bIfPreview = false;
    private int iCurrentCameraId = 0;
    private SurfaceHolder currentHolder = null;
    private final int iCaptureBuffers = 3;

    private int mCameraOrientation = 0;
    private int mCameraFacing = 0;
    private int mDeviceOrientation = 0;
    private OnSurfaceViewCallback mSurfaceViewCallback;

    public interface OnSurfaceViewCallback {
        void onSurfaceCreated();
    }

    public void setOnSurfaceViewCallback(OnSurfaceViewCallback callback) {
        mSurfaceViewCallback = callback;
    }

    // 初始化摄像机，在surfaceCreated中调用
    private void initCamera() {
        if (null == mCamera)
            return;
        try {
            if (bIfPreview) {
                mCamera.stopPreview();// stopCamera();
                mCamera.setPreviewCallbackWithBuffer(null);
            }
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(iCurrentCameraId, cameraInfo);
            mCameraOrientation = cameraInfo.orientation;
            mCameraFacing = cameraInfo.facing;
            mDeviceOrientation = getDeviceOrientation();
            Log.i(TAG, "allocate: device orientation=" + mDeviceOrientation + ", camera orientation=" + mCameraOrientation + ", facing=" + mCameraFacing);

            setCameraDisplayOrientation();

            /* Camera Service settings */
            Camera.Parameters parameters = mCamera.getParameters();
            mPrviewSizeList = parameters.getSupportedPreviewSizes();
            mVideoSizeList = parameters.getSupportedVideoSizes();
            // 获取camera支持的相关参数，判断是否可以设置
            List<Size> previewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Collections.sort(previewSizes, new CameraSizeComparator());
            // 获取当前设置的分辩率参数
            int index = bestVideoSize(mVideoSizeList, mPrviewSizeList.get(0).width);
            boolean bSetPreviewSize = false;
//            if (previewSizes.size() == 1) {
//                bSetPreviewSize = true;
//                parameters.setPreviewSize(previewSizes.get(0).width, previewSizes.get(0).height);
//            } else {
//                parameters.setPreviewSize(mVideoSizeList.get(index).width, mVideoSizeList.get(index).height);
//                for (int i = 0; i < mVideoSizeList.size(); i++) {
//                    L.e("---------------param:"+mVideoSizeList.get(i).width);
//                }
//            }
            int maxWidth = 0;
            int maxHeight = 0;
            if (previewSizes.size() >= 1) {
                bSetPreviewSize = true;
                for (int i = 0; i < previewSizes.size(); i++) {
                    int width = previewSizes.get(i).width;
                    if (maxWidth < width) {
                        maxWidth = width;
                    }
                    int height = previewSizes.get(i).height;
                    if (maxHeight < height)
                        maxHeight = height;
                }
                parameters.setPreviewSize(maxWidth, maxHeight);
            }
            // 指定的分辩率不支持时，如果当前手机支持320x240分辨率，优选设置320x240分辨率否则使用手机支持分辨率中最低的分辨率进行设置
//            if (!bSetPreviewSize) {
//                if (previewSizes.size() > 0) {
//                    Size s = previewSizes.get(0);
//                    parameters.setPreviewSize(s.width, s.height);
//                }
//            }

            // 设置视频采集帧率
            List<int[]> fpsRange = parameters.getSupportedPreviewFpsRange();
            for (int i = 0; i < fpsRange.size(); i++) {
                int[] r = fpsRange.get(i);
                if (r[0] >= 25000 && r[1] >= 25000) {
                    parameters.setPreviewFpsRange(r[0], r[1]);
                    break;
                }
            }

            // 设置视频数据格式
            parameters.setPreviewFormat(ImageFormat.NV21);
            // 参数设置生效
            try {
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Size captureSize = mCamera.getParameters().getPreviewSize();
            int bufSize = captureSize.width * captureSize.height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
            for (int i = 0; i < iCaptureBuffers; i++) {
                mCamera.addCallbackBuffer(new byte[bufSize]);
            }
            // 设置视频输出回调函数，通过AnyChat的外部视频输入接口传入AnyChat内核进行处理
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mCamera != null)
                        mCamera.addCallbackBuffer(data);
                }
            });
            mCamera.startPreview(); // 打开预览画面
            bIfPreview = true;
            int iCurPreviewRange[] = new int[2];
            parameters.getPreviewFpsRange(iCurPreviewRange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CameraSizeComparator implements Comparator<Size> {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return Integer.compare(lhs.height, rhs.height);
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    // 关闭摄像头
    public void CloseCamera() {
        try {
            if (null != mCamera) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                bIfPreview = false;
                mCamera.release();
                mCamera = null;
            }
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 获取系统中摄像头的数量
    public int GetCameraNumber() {
        try {
            return Camera.getNumberOfCameras();
        } catch (Exception ex) {
            return 0;
        }
    }

    // 自动对焦
    public void CameraAutoFocus() {
        if (mCamera == null || !bIfPreview)
            return;
        try {
            mCamera.autoFocus(null);
        } catch (Exception ex) {

        }
    }

    // 切换摄像头
    public void SwitchCamera() {
        try {
            if (Camera.getNumberOfCameras() == 1 || currentHolder == null)
                return;
            iCurrentCameraId = (iCurrentCameraId == 0) ? 1 : 0;
            if (null != mCamera) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                bIfPreview = false;
                mCamera.release();
                mCamera = null;
            }

            mCamera = Camera.open(iCurrentCameraId);
            mCamera.setPreviewDisplay(currentHolder);
            initCamera();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    // 根据摄像头的方向选择摄像头（前置、后置）
    public void SelectVideoCapture(int facing) {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facing) {
                iCurrentCameraId = i;
                break;
            }
        }
    }

    // 根据摄像头的序号选择摄像头（0 - GetCameraNumber()）
    public void SelectCamera(int iCameraId) {
        try {
            if (Camera.getNumberOfCameras() <= iCameraId || currentHolder == null)
                return;
            if (null != mCamera && iCurrentCameraId == iCameraId)
                return;
            iCurrentCameraId = iCameraId;
            if (null != mCamera) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                bIfPreview = false;
                mCamera.release();
                mCamera = null;
            }

            mCamera = Camera.open(iCameraId);
            mCamera.setPreviewDisplay(currentHolder);
            initCamera();
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera = Camera.open(iCurrentCameraId);
            currentHolder = holder;
            mCamera.setPreviewDisplay(holder);//set the surface to be used for live preview
            initCamera();
            if (mSurfaceViewCallback != null) {
                mSurfaceViewCallback.onSurfaceCreated();
            }
        } catch (Exception ex) {
            if (null != mCamera) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    public void takePicture(Camera.PictureCallback callback) {
        try {
            mCamera.takePicture(null, null, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != mCamera) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                bIfPreview = false;
                mCamera.release();
                mCamera = null;
            } catch (Exception ex) {
                mCamera = null;
                bIfPreview = false;
            }
        }
        currentHolder = null;
    }

    private int getDeviceOrientation() {
        int orientation = 0;
        WindowManager wm = (WindowManager) Util.getApp().getSystemService(Context.WINDOW_SERVICE);
        //Log.i(TAG, "wm.getDefaultDisplay().getRotation():" + wm.getDefaultDisplay().getRotation());
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
            case Surface.ROTATION_0:
            default:
                orientation = 0;
                break;
        }
        return orientation;
    }

    private void setCameraDisplayOrientation() {
        try {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(iCurrentCameraId, cameraInfo);

            WindowManager wm = (WindowManager) Util.getApp().getSystemService(Context.WINDOW_SERVICE);
            assert wm != null;
            int rotation = wm.getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (cameraInfo.orientation + degrees) % 360;
                result = (360 - result) % 360;   // compensate the mirror
            } else {   // back-facing
                result = (cameraInfo.orientation - degrees + 360) % 360;
            }

            mCamera.setDisplayOrientation(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private MediaRecorder mRecorder;
    private List<Size> mPrviewSizeList;
    private List<Size> mVideoSizeList;

    public interface OnRecordListener {
        void onRecordCompleted();
    }

    public void stopRecord() {
        if (mRecorder != null)
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        mRecorder = null;
        mIsRecording = false;
    }

    private boolean mIsRecording = false;

    public boolean isRecording() {
        return mIsRecording;
    }

    public void startRecord(DoorbellConfig config, String type, final OnRecordListener listener) {
        File saveFile;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Util.getApp().getResources().getConfiguration().locale);
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        final String tempPath = FileUtil.getDoorbellVideoPath() + File.separator +
                "VID_" + time + ".mp4";
        saveFile = new File(tempPath);
        final int recordTime;
        if (DoorbellSystemAction.TYPE_DOORBELL_SYSTEM_RING.equals(type)) {
            if (config.getDoorbellVideotap() == 1) {
                recordTime = config.getVideotapTime();
            } else {
                //留言
                recordTime = config.getVideoLeaveMsgTime();
                int count = ControlCenter.getDoorbellManager().getDoorbellLeaveMsgCount() + 1;
                ControlCenter.getDoorbellManager().setDoorbellLeaveMsgCount(count);
            }
        } else {
            recordTime = config.getVideotapTime();
        }
        mRecorder = new MediaRecorder();
        initCamera();
        if (mCamera == null) {
            if (listener != null) {
                listener.onRecordCompleted();
            }
            return;
        }
        mCamera.unlock();
        mRecorder.setCamera(mCamera);
        mRecorder.reset();
        int index = bestVideoSize(mVideoSizeList, mPrviewSizeList.get(0).width);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置采集声音
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置采集图像
        //2.设置视频，音频的输出格式
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //3.设置音频的编码格式
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //设置图像的编码格式
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setVideoSize(mVideoSizeList.get(index).width, mVideoSizeList.get(index).height);
        mRecorder.setOutputFile(saveFile.getAbsolutePath());
        mRecorder.setPreviewDisplay(currentHolder.getSurface());
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Handler handler = new Handler();
        //开始录制
        try {
            mRecorder.start();
            mIsRecording = true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(recordTime * 1000);
                    if (mRecorder != null)//用户手动结束主线程时，已设为null
                        try {
                            mRecorder.stop();
                            mIsRecording = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    mRecorder = null;
                    if (listener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onRecordCompleted();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //查找出最接近的视频录制分辨率
    private int bestVideoSize(List<Size> videoSizeList, int _w) {
        //降序排列
        Collections.sort(videoSizeList, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                if (lhs.width > rhs.width) {
                    return -1;
                } else if (lhs.width == rhs.width) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (int i = 0; i < videoSizeList.size(); i++) {
            if (videoSizeList.get(i).width < _w) {
                return i;
            }
        }
        return 0;
    }
}