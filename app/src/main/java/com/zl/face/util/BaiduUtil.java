//package com.zl.face.util;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.baidu.aip.api.FaceApi;
//import com.baidu.aip.db.DBManager;
//import com.baidu.aip.entity.ARGBImg;
//import com.baidu.aip.entity.Feature;
//import com.baidu.aip.entity.Group;
//import com.baidu.aip.entity.IdentifyRet;
//import com.baidu.aip.entity.User;
//import com.baidu.aip.manager.FaceDetector;
//import com.baidu.aip.manager.FaceLiveness;
//import com.baidu.aip.manager.FaceSDKManager;
//import com.baidu.aip.utils.FeatureUtils;
//import com.baidu.aip.utils.PreferencesUtil;
//import com.baidu.idl.facesdk.FaceInfo;
//
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class BaiduUtil {
//    private static final String TAG = "Baidu";
//    private static final int FEATURE_DATAS_UNREADY = 1;
//    private static final int IDENTITY_IDLE = 2;
//    private static final int IDENTITYING = 3;
//    private static ExecutorService es = Executors.newSingleThreadExecutor();
//    private static volatile int identityStatus = FEATURE_DATAS_UNREADY;
//    private static String groupId;
//
//    public static void init(Context mContext) {
//        PreferencesUtil.initPrefs(mContext);
//        DBManager.getInstance().init(mContext);
//    }
//
//    public static void loadFeature2Memery(String groupIds) {
//        if (identityStatus != FEATURE_DATAS_UNREADY) {
//            return;
//        }
//        BaiduUtil.groupId = groupIds;
//        es.submit(new Runnable() {
//            @Override
//            public void run() {
//                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
//                // android.os.Process.setThreadPriority (-4);
//                FaceApi.getInstance().loadFacesFromDB(groupId);
//                Log.i(TAG, "人脸数据加载完成，即将开始1：N");
//                int count = FaceApi.getInstance().getGroup2Facesets().get(groupId).size();
//                Log.i(TAG, "底库人脸个数：" + count);
//                identityStatus = IDENTITY_IDLE;
//            }
//        });
//    }
//
//    public static void asyncIdentity(final ARGBImg argbImg, final FaceInfo faceInfo) {
//        if (identityStatus != IDENTITY_IDLE) {
//            return;
//        }
//        es.submit(new Runnable() {
//            @Override
//            public void run() {
//                if (faceInfo == null) {
//                    return;
//                }
//                identity(argbImg, faceInfo);
//            }
//        });
//    }
//
//    public static void identity(ARGBImg argbImg, FaceInfo faceInfo) {
//        if (TextUtils.isEmpty(groupId)) return;
//        float raw = Math.abs(faceInfo.headPose[0]);
//        float patch = Math.abs(faceInfo.headPose[1]);
//        float roll = Math.abs(faceInfo.headPose[2]);
//        // 人脸的三个角度大于20不进行识别
//        if (raw > 20 || patch > 20 || roll > 20) {
//            return;
//        }
//        identityStatus = IDENTITYING;
//        long starttime = System.currentTimeMillis();
//        int[] argb = argbImg.data;
//        int rows = argbImg.height;
//        int cols = argbImg.width;
//        int[] landmarks = faceInfo.landmarks;
//        IdentifyRet identifyRet = FaceApi.getInstance().identity(argb, rows, cols, landmarks, groupId);
//        User user = FaceApi.getInstance().getUserInfo(groupId, identifyRet.getUserId());
//        Log.i(TAG, "user:" + user.toString());
//        identityStatus = IDENTITY_IDLE;
//        Log.i(TAG, "特征抽取对比耗时:" + (System.currentTimeMillis() - starttime));
//    }
//
//
//    public static void detect(Context mContext) {
//        Bitmap bitmap = FileUtil.getAssetPic(mContext, "zl.png");
//        if (null == bitmap) return;
//        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
//        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
//        FaceSDKManager.getInstance().getFaceDetector().detect(argbImg.data, argbImg.width, argbImg.height);
//        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().getTrackedFaces();
//        if (faceInfos != null && faceInfos.length > 0) {
//            for (FaceInfo faceInfo : faceInfos) {
////                Log.i(TAG, "mWidth:" + faceInfo.mWidth + "mConf:" + faceInfo.mConf
////                        + "mCenter_y:" + faceInfo.mCenter_y + "mCenter_x:" + faceInfo.mCenter_x +
////                        "mAngle:" + faceInfo.mAngle + "is_live_mouth:" + faceInfo.is_live_mouth() +
////                        "is_live_head_up:" + faceInfo.is_live_head_up() + "is_live_head_turn_right:" + faceInfo.is_live_head_turn_right() +
////                        "is_live_head_turn_left:" + faceInfo.is_live_head_turn_left() + "is_live_head_down:" + faceInfo.is_live_head_down() +
////                        "is_live:" + faceInfo.is_live() + "rightEyeState:" + faceInfo.get_rightEyeState() +
////                        "mouthState:" + faceInfo.get_mouthState() + "leftEyeState:" + faceInfo.get_leftEyeState());
////                Bitmap sbitmap = FaceCropper.getFace(argbImg.data, faceInfo, argbImg.width);
////                FileUtil.saveBitmapFile(sbitmap);
////                Rect faceRect = FaceCropper.getFaceRect(argbImg.data, faceInfo, argbImg.width);
////                Log.i(TAG, "faceRect：" + faceRect.toString());
//                float rgbScore = FaceLiveness.getInstance().rgbLiveness(argbImg.data, argbImg.width, argbImg.height, faceInfo.landmarks);
//                Log.i(TAG, "RGB活体得分：" + rgbScore);
//                asyncIdentity(argbImg, faceInfo);
//            }
//
//        }
//        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
//    }
//
//
//    public static boolean addGroup(String groupIds, String desc) {
//        if (TextUtils.isEmpty(groupIds)) {
//            return false;
//        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(groupIds);
//        if (!matcher.matches()) {
//            Log.i(TAG, "groupId、字母、下划线中的一个或者多个组合");
//            return false;
//        }
//        Group group = new Group();
//        group.setGroupId(groupIds);
//        group.setDesc(desc);
//        boolean ret = FaceApi.getInstance().groupAdd(group);
//        return ret;
//    }
//
//    public static void addUser(String groupIds, String username, final String imageName, final ARGBImg argbImg) {
//        if (TextUtils.isEmpty(username)) {
//            Log.i(TAG, "userid不能为空");
//            return ;
//        }
//        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
//        Matcher matcher = pattern.matcher(username);
//        if (!matcher.matches()) {
//            Log.i(TAG, "userid由数字、字母、下划线中的一个或者多个组合");
//            return ;
//        }
//        if (TextUtils.isEmpty(groupIds)) {
//            Log.i(TAG, "分组groupId为空");
//            return ;
//        }
//        matcher = pattern.matcher(username);
//        if (!matcher.matches()) {
//            Log.i(TAG, "groupId由数字、字母、下划线中的一个或者多个组合");
//            return ;
//        }
//        /*
//         * 用户id（由数字、字母、下划线组成），长度限制128B
//         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
//         *
//         */
//        final String uid = UUID.randomUUID().toString();
//        // String uid = 修改为自己用户系统中用户的id;
//        final User user = new User();
//        user.setUserId(uid);
//        user.setUserInfo(username);
//        user.setGroupId(groupIds);
//
//        Executors.newSingleThreadExecutor().submit(new Runnable() {
//
//            @Override
//            public void run() {
//                byte[] bytes = new byte[2048];
//                int ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg, bytes, 50);
//                if (ret == FaceDetector.NO_FACE_DETECTED) {
//                    Log.i(TAG, "人脸太小（必须打于最小检测人脸minFaceSize），或者人脸角度太大，人脸不是朝上");
//                } else if (ret != -1) {
//                    Feature feature = new Feature();
//                    feature.setGroupId(groupId);
//                    feature.setUserId(uid);
//                    feature.setFeature(bytes);
//                    feature.setImageName(imageName);
//                    user.getFeatureList().add(feature);
//                    if (FaceApi.getInstance().userAdd(user)) {
//                        Log.i(TAG, "注册成功");
//                    } else {
//                        Log.i(TAG, "注册失败");
//                    }
//                } else {
//                    Log.i(TAG, "抽取特征失败");
//                }
//            }
//        });
//    }
//
//
//
//}
