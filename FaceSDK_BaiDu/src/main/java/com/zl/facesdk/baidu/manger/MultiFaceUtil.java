package com.zl.facesdk.baidu.manger;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.zl.facesdk.baidu.db.DBManager;
import com.zl.facesdk.baidu.entity.ARGBImg;
import com.zl.facesdk.baidu.entity.Feature;
import com.zl.facesdk.baidu.entity.IdentifyRet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MultiFaceUtil {
    private static final String TAG = "MultiFaceUtil";
    private static int MAX_THREAD_NUM = 2;
    private ArrayList<FaceTracker> outWorkTrackers;
    private ArrayList<FaceTracker> inWorkTrackers;
    private FaceEnvironment faceEnvironment = new FaceEnvironment();
    private HashMap<String, byte[]> features = new HashMap<>();

    public static void setMaxThreadNum(int maxThreadNum) {
        MAX_THREAD_NUM = maxThreadNum;
    }

    public void init(Context mContext) {
        inWorkTrackers = new ArrayList<>(MAX_THREAD_NUM);
        outWorkTrackers = new ArrayList<>(MAX_THREAD_NUM);
        for (int i = 0; i < MAX_THREAD_NUM; i++) {
            FaceTracker mFaceTracker = new FaceTracker(mContext);
            mFaceTracker.set_isFineAlign(false);
            mFaceTracker.set_isVerifyLive(false);
            mFaceTracker.set_DetectMethodType(1);
            outWorkTrackers.add(mFaceTracker);
        }
        // 人脸可见光离线识别模型初始化，通过index调用FaceSDK.extractFeature方法提取人脸特征值
        FaceSDK.recognizeModelInit(mContext, MAX_THREAD_NUM, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
        // 人脸属性模型初始化，通过index 调用FaceSDK.faceAttribute方法获取人脸属性提取
        // FaceSDK.faceAttributeModelInit(this, MAX_THREAD_NUM);
        // 人脸可见光静默活体模型初始化，通过index 调用FaceSDK.run_livenessSilentPredict方法获取静默活体分值，
        FaceSDK.livenessSilentInit(mContext, FaceSDK.LivenessTypeId.LIVEID_VIS, MAX_THREAD_NUM);
        // 超分辨率模型初始化，通过FaceSDK.superResolution 方法将模糊图片处理为高清图片
        //  FaceSDK.superResolutionMoelInit(this, MAX_THREAD_NUM);
        // 去网纹模型初始化，通过FaceSDK.removeTexture 方法将身份证上的条纹擦除掉
        // FaceSDK.removeTextureModelInit(this, MAX_THREAD_NUM);
        // 人脸tacker 单个功能模型初始化，通过index 调用FaceSDK.detect，FaceSDK.align，FaceSDK.imgQuality单个功能方法
//         FaceSDK.initModel(this, MAX_THREAD_NUM);
    }

    private FaceInfo[] faceTrackerWork(FaceTracker faceTracker, ARGBImg argbImg) {
        if (null == argbImg) return null;
        faceTracker.clearTrackedFaces();
        long startTime = System.currentTimeMillis();
        FaceTracker.ErrCode errCode = faceTracker.faceVerification(argbImg.data, argbImg.height, argbImg.width, FaceSDK.ImgType.ARGB, FaceTracker.ActionType.RECOGNIZE);
        long rgbDetectDuration = System.currentTimeMillis() - startTime;
        Log.e(TAG, "人脸检测的时间" + rgbDetectDuration);
        if (errCode != FaceTracker.ErrCode.OK) return null;
        FaceInfo[] faceInfos = faceTracker.get_TrackedFaceInfo();
        return faceInfos;
    }

    private byte[] extractFeature(ARGBImg argbImg, FaceInfo faceInfo) {
        long startTime2 = System.currentTimeMillis();
        byte[] feature = new byte[2048];
        FaceSDK.extractFeature(getFeatureIndex(), argbImg.data, argbImg.height, argbImg.width, FaceSDK.ImgType.ARGB.ordinal(), feature, faceInfo.landmarks, 1, FaceSDK.RecognizeType.RECOGNIZE_LIVE.ordinal());
        long extractFeatureDuration = System.currentTimeMillis() - startTime2;
        Log.e(TAG, "特征抽取的时间" + extractFeatureDuration);
        return feature;
    }

    private int featureIndex = -1;

    private synchronized int getFeatureIndex() {
        if (featureIndex >= MAX_THREAD_NUM - 1) {
            featureIndex = -1;
        }
        featureIndex = featureIndex + 1;
        return featureIndex;
    }

    private int liveIndex = -1;

    private synchronized int getLiveIndex() {
        if (liveIndex >= MAX_THREAD_NUM - 1) {
            liveIndex = -1;
        }
        liveIndex = liveIndex + 1;
        return liveIndex;
    }


    private float getLive(ARGBImg argbImg, FaceInfo faceInfo) {
        long startTime3 = System.currentTimeMillis();
        float score = FaceSDK.run_livenessSilentPredict(FaceSDK.LivenessTypeId.LIVEID_VIS, getLiveIndex()cvD, argbImg.data, argbImg.height, argbImg.width, 24, faceInfo.landmarks);
        long livenessDuration = System.currentTimeMillis() - startTime3;
        Log.e(TAG, "活体检测的时间" + livenessDuration);
        return score;
    }

    public IdentifyRet identity(byte[] imgFeature) {
        String userIdOfMaxScore = "";
        float identifyScore = 0;
        Iterator iterator = features.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry<String, byte[]>) iterator.next();
            byte[] feature = entry.getValue();
            float score = FaceSDK.getFaceSimilarity(feature, feature, 1, FaceSDK.RecognizeType.RECOGNIZE_LIVE.ordinal());
            if (score > identifyScore) {
                identifyScore = score;
                userIdOfMaxScore = entry.getKey();
            }
        }
        return new IdentifyRet(userIdOfMaxScore, identifyScore);
    }


    public void loadFacesFromDB(String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        if (features != null) {
            features.clear();
            features = new HashMap<>();
        }
        List<Feature> featureList = DBManager.getInstance().queryFeatureByGroupId(groupId);
        if (featureList != null && featureList.size() > 0) {
            for (Feature feature : featureList) {
                addFeature(feature);
            }
        }
    }

    public synchronized void addFeature(Feature feature) {
        if (null == feature || null == features) return;
        features.put(feature.getUserId(), feature.getFeature());
    }


    public FaceEnvironment getFaceEnvironment() {
        return faceEnvironment;
    }

    public void setFaceEnvironment(FaceEnvironment faceEnvironment) {
        this.faceEnvironment = faceEnvironment;
        if (null != inWorkTrackers) {
            for (FaceTracker inWorkTracker : inWorkTrackers) {
                setTrackerEnvironment(inWorkTracker, faceEnvironment);
            }
        }
        if (null != outWorkTrackers) {
            for (FaceTracker outWorkTracker : outWorkTrackers) {
                setTrackerEnvironment(outWorkTracker, faceEnvironment);
            }
        }
    }

    private static void setTrackerEnvironment(FaceTracker mFaceTracker, FaceEnvironment environment) {
        if (null == environment) return;
        mFaceTracker.set_isCheckQuality(environment.isCheckQuality());
        mFaceTracker.set_isCheckQuality(environment.isCheckQuality());
        mFaceTracker.set_notFace_thr(environment.getNotFaceThreshold());
        mFaceTracker.set_min_face_size(environment.getMiniFaceSize());
        mFaceTracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        mFaceTracker.set_illum_thr(environment.getIlluminationThreshold());
        mFaceTracker.set_blur_thr(environment.getBlurrinessThreshold());
        mFaceTracker.set_occlu_thr(environment.getOcclulationThreshold());
        mFaceTracker.set_max_reg_img_num(FaceEnvironment.VALUE_MAX_CROP_IMAGE_NUM);
        mFaceTracker.set_eulur_angle_thr(environment.getPitch(), environment.getYaw(), environment.getRoll());
    }


}
