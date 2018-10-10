package com.zl.facesdk.baidu.manger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.license.AndroidLicenser;
import com.zl.facesdk.baidu.db.DBManager;
import com.zl.facesdk.baidu.util.FileUitls;
import com.zl.facesdk.baidu.util.PreferencesUtil;

public class FaceSDKManager {
    public static final int SDK_UNACTIVATION = 1;
    public static final int SDK_UNINIT = 2;
    public static final int SDK_INITING = 3;
    public static final int SDK_INITED = 4;
    public static final int SDK_FAIL = 5;
    public static final String LICENSE_NAME = "idl-license.face-android";
    private FaceDetector faceDetector;
    private FaceFeature faceFeature;
    public static volatile int initStatus = SDK_UNACTIVATION;

    private FaceSDKManager() {
        faceDetector = new FaceDetector();
        faceFeature = new FaceFeature();
    }

    private static class HolderClass {
        private static final FaceSDKManager instance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.instance;
    }

    public FaceDetector getFaceDetector() {
        return faceDetector;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }


    public void init(Context mContext, String key, boolean onlyDetect, SdkInitListener sdkInitListener) {
        PreferencesUtil.initPrefs(mContext);
        if (!onlyDetect)
            DBManager.getInstance().init(mContext);
        FaceSDKActivate.setActivateKey(key);
        FaceSDKActivate.activate(mContext,new FaceSDKActivate.SdkActivateListener() {
            @Override
            public void activateSuccess() {
                inintFaceSDK(mContext, sdkInitListener);
            }

            @Override
            public void activateFail(int errorCode, String msg) {
                sdkInitListener.initFail(errorCode, msg);
            }
        });
    }

    /**
     * FaceSDK 初始化，用户可以根据自己的需求实例化FaceTracker 和 FaceRecognize
     *
     * @param mContext
     */
    public void inintFaceSDK(Context mContext, SdkInitListener sdkInitListener) {
        if (null == sdkInitListener) return;
        if (!check(mContext)) {
            initStatus = SDK_UNACTIVATION;
            sdkInitListener.initFail(-1, "license文件不存在");
            return;
        }
        final String key = PreferencesUtil.getString("activate_key", "");
        if (TextUtils.isEmpty(key)) {
            Log.e("FaceSDK", "激活序列号为空, 请先激活");
            sdkInitListener.initFail(-1, "激活序列号为空, 请先激活");
            return;
        }
        initStatus = SDK_INITING;
        sdkInitListener.initStart();
        Log.e("FaceSDK", "初始化授权");
        FaceSDK.initLicense(mContext, key, LICENSE_NAME, false);
        if (!sdkInitStatus(sdkInitListener)) {
            return;
        }
        Log.e("FaceSDK", "初始化sdk");
        faceDetector.init(mContext);
        faceFeature.init(mContext,FaceSDK.RecognizeType.RECOGNIZE_LIVE);
        initLiveness(mContext);
        sdkInitListener.initSuccess();
    }

    /**
     * 初始化 活体检测
     *
     * @param context
     */
    private void initLiveness(Context context) {
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_VIS, 2);
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_IR);
        FaceSDK.livenessSilentInit(context, FaceSDK.LivenessTypeId.LIVEID_DEPTH);
    }


    private boolean sdkInitStatus(SdkInitListener sdkInitListener) {
        if (null == sdkInitListener) return false;
        boolean success = false;
        int status = FaceSDK.getAuthorityStatus();
        if (status == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
            initStatus = SDK_INITED;
            success = true;
            faceDetector.setInitStatus(initStatus);
            Log.e("FaceSDK", "授权成功");
        } else if (status == AndroidLicenser.ErrorCode.LICENSE_EXPIRED.ordinal()) {
            initStatus = SDK_FAIL;
            Log.e("FaceSDK", "授权过期");
            sdkInitListener.initFail(status, "授权过期");
        } else {
            initStatus = SDK_FAIL;
            Log.e("FaceSDK", "授权失败" + status);
            sdkInitListener.initFail(status, "授权失败");
        }
        return success;
    }


    public boolean check(Context mContext) {
        if (!FileUitls.checklicense(mContext, LICENSE_NAME)) {
            return false;
        } else {
            return true;
        }
    }


    public interface SdkInitListener {

        void initStart();

        void initSuccess();

        void initFail(int errorCode, String msg);
    }


}
