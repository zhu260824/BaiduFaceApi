package com.zl.facesdk.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.facesdk.FaceInfo;
import com.zl.facesdk.baidu.api.FaceApi;
import com.zl.facesdk.baidu.db.DBManager;
import com.zl.facesdk.baidu.entity.ARGBImg;
import com.zl.facesdk.baidu.entity.AddResult;
import com.zl.facesdk.baidu.entity.Feature;
import com.zl.facesdk.baidu.entity.Group;
import com.zl.facesdk.baidu.entity.IdentifyRet;
import com.zl.facesdk.baidu.entity.User;
import com.zl.facesdk.baidu.manger.FaceDetector;
import com.zl.facesdk.baidu.manger.FaceLiveness;
import com.zl.facesdk.baidu.manger.FaceSDKManager;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FaceSDKUtil {
    private static final String TAG = "Baidu";
    private static final int FEATURE_DATAS_UNREADY = 1;
    private static final int IDENTITY_IDLE = 2;
    private static final int IDENTITYING = 3;
    private static volatile int identityStatus = FEATURE_DATAS_UNREADY;
    private static String groupId;

    public FaceSDKUtil() {
    }

    public static void init(Context mContext, String key, boolean onlyDetect, FaceSDKManager.SdkInitListener sdkInitListener) {
        FaceSDKManager.getInstance().init(mContext, key, onlyDetect, sdkInitListener);
    }

    public static void setGroupId(String groupId) {
        FaceSDKUtil.groupId = groupId;
    }

    public static void loadFeature2Memery(String groupId) {
        if (identityStatus != FEATURE_DATAS_UNREADY) {
            return;
        }
        FaceSDKUtil.groupId = groupId;
        FaceApi.getInstance().loadFacesFromDB(groupId);
        Log.i(TAG, "人脸数据加载完成，即将开始1：N");
        int count = FaceApi.getInstance().getGroup2Facesets().get(groupId).size();
        Log.i(TAG, "底库人脸个数：" + count);
        identityStatus = IDENTITY_IDLE;
    }

    public static IdentifyRet identity(ARGBImg argbImg, FaceInfo faceInfo) {
        if (identityStatus != IDENTITY_IDLE || null == argbImg || null == faceInfo) {
            return null;
        }
        if (TextUtils.isEmpty(groupId)) return null;
        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        // 人脸的三个角度大于20不进行识别
        if (raw > 20 || patch > 20 || roll > 20) {
            return null;
        }
        identityStatus = IDENTITYING;
        long starttime = System.currentTimeMillis();
        int[] argb = argbImg.data;
        int rows = argbImg.height;
        int cols = argbImg.width;
        int[] landmarks = faceInfo.landmarks;
        IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, groupId);
        identityStatus = IDENTITY_IDLE;
        Log.i(TAG, "特征抽取对比耗时:" + (System.currentTimeMillis() - starttime));
        return identifyRet;
    }


    public static FaceInfo[] detect(ARGBImg argbImg) {
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        FaceSDKManager.getInstance().getFaceDetector().detect(argbImg.data, argbImg.width, argbImg.height);
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().getTrackedFaces();
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        return faceInfos;
    }

    public static float rgbLive(ARGBImg argbImg, FaceInfo faceInfo) {
        if (null == argbImg || null == faceInfo) return 0;
        return FaceLiveness.getInstance().rgbLiveness(argbImg.data, argbImg.width, argbImg.height, faceInfo.landmarks);
    }


    public static AddResult addGroup(String groupId, String desc) {
        if (TextUtils.isEmpty(groupId)) {
            return AddResult.getNotNull("groupId不能为空");
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(groupId);
        if (!matcher.matches()) {
            return AddResult.getNotRule("groupId由数字、字母、下划线中的一个或者多个组合");
        }
        Group group = new Group();
        group.setGroupId(groupId);
        group.setDesc(desc);
        boolean ret = FaceApi.getInstance().groupAdd(group);
        if (ret) {
            return AddResult.getSuccess();
        } else {
            return AddResult.getErroeDB("数据库添加错误");
        }
    }


    public static AddResult addUser(String groupId, String username, String imageName, ARGBImg argbImg) {
        if (TextUtils.isEmpty(username)) {
            return AddResult.getNotNull("userid不能为空");
        }
        if (TextUtils.isEmpty(groupId)) {
            return AddResult.getNotNull("groupId不能为空");
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(username);
        if (!matcher.matches()) {
            return AddResult.getNotRule("userid由数字、字母、下划线中的一个或者多个组合");
        }
        matcher = pattern.matcher(username);
        if (!matcher.matches()) {
            return AddResult.getNotRule("username由数字、字母、下划线中的一个或者多个组合");
        }
        final String uid = UUID.randomUUID().toString();
        final User user = new User();
        user.setUserId(uid);
        user.setUserInfo(username);
        user.setGroupId(groupId);
        byte[] bytes = new byte[2048];
        int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
        if (ret == FaceDetector.NO_FACE_DETECTED) {
            return AddResult.getNotRule("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
        } else if (ret != -1) {
            Feature feature = new Feature();
            feature.setGroupId(groupId);
            feature.setUserId(uid);
            feature.setFeature(bytes);
            feature.setImageName(imageName);
            user.getFeatureList().add(feature);
            if (FaceApi.getInstance().userAdd(user)) {
                return AddResult.getSuccess();
            } else {
                return AddResult.getErroeDB("数据库添加错误");
            }
        } else {
            return AddResult.getNotRule("图片抽取特征值失败");
        }
    }


    public static AddResult addFeaturer(String groupId, String userId, String imageName, ARGBImg argbImg) {
        if (TextUtils.isEmpty(userId)) {
            return AddResult.getNotNull("userid不能为空");
        }
        if (TextUtils.isEmpty(groupId)) {
            return AddResult.getNotNull("groupId不能为空");
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(userId);
        if (!matcher.matches()) {
            return AddResult.getNotRule("userid由数字、字母、下划线中的一个或者多个组合");
        }
        byte[] bytes = new byte[2048];
        int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
        if (ret == FaceDetector.NO_FACE_DETECTED) {
            return AddResult.getNotRule("人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
        } else if (ret != -1) {
            Feature feature = new Feature();
            feature.setGroupId(groupId);
            feature.setUserId(userId);
            feature.setFeature(bytes);
            feature.setImageName(imageName);
            if (FaceApi.getInstance().addFeature(feature)) {
                return AddResult.getSuccess();
            } else {
                return AddResult.getErroeDB("数据库添加错误");
            }
        } else {
            return AddResult.getNotRule("图片抽取特征值失败");
        }
    }

    public static AddResult deleteFeaturer(String groupId, String userId, String faceToken) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(groupId) || TextUtils.isEmpty(faceToken)) {
            return AddResult.getNotNull("userid或groupId或faceToken不能为空");
        }
        boolean ret = DBManager.getInstance().deleteFeature(userId, groupId, faceToken);
        if (ret) {
            return AddResult.getSuccess();
        } else {
            return AddResult.getErroeDB("数据库添加错误");
        }
    }

}
