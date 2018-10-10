//package com.zl.face.util;
//
//import android.content.Context;
//import android.os.Environment;
//import android.text.TextUtils;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.zl.facesdk.baidu.manger.FaceSDKManager;
//import com.zl.facesdk.baidu.util.PreferencesUtil;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//
//public class BaiduActive {
//    private static final String TAG = "Baidu";
//
//    public static void setBaiduKey(String key) {
//        if (TextUtils.isEmpty(key)) return;
//        PreferencesUtil.putString("activate_key", key);
//    }
//
//    public static void active(Context mContext) {
//        if (FaceSDK.getAuthorityStatus() == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
//            //已经激活成功
//            return;
//        }
//        FaceSDKManager.getInstance().init(mContext);
//        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
//            @Override
//            public void initStart() {
//                Log.i(TAG, "sdk init start");
//            }
//
//            @Override
//            public void initSuccess() {
//                Log.i(TAG, "sdk init success");
//            }
//
//            @Override
//            public void initFail(int errorCode, String msg) {
//                Log.i(TAG, "sdk init fail:" + msg);
//            }
//        });
//    }
//
//    public static void offLineActive(Context mContext) {
//        if (FaceSDK.getAuthorityStatus() == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
//            Toast.makeText(mContext, "已经激活成功", Toast.LENGTH_LONG).show();
//            return;
//        }
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String firstPath = path + File.separator + "License.zip";
//        if (fileIsExists(firstPath)) {
//            if (!TextUtils.isEmpty(firstPath)) {
//                ZipUtil.unzip(firstPath);
//            }
//            if (ZipUtil.isSuccess) {
//                String secondPath = path + "/" + "Win.zip";
//                if (!TextUtils.isEmpty(secondPath)) {
//                    ZipUtil.unzip(secondPath);
//                }
//            }
//            String keyPath = path + "/" + "license.key";
//            String key = readFile(keyPath, "key");
//            PreferencesUtil.putString("activate_key", key);
//            String liscensePaht = path + "/" + "license.ini";
//            String liscense = readFile(liscensePaht, "liscense");
//            success = FileUitls.c(mContext, FaceSDKManager.LICENSE_NAME, list);
//            if (success) {
//                Log.i(TAG, "激活成功");
//                FaceSDKManager.initStatus = FaceSDKManager.SDK_UNINIT;
//                FaceSDKManager.getInstance().init(mContext);
//                FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
//                    @Override
//                    public void initStart() {
//                        Log.i(TAG, "sdk init start");
//                    }
//
//                    @Override
//                    public void initSuccess() {
//                        Log.i(TAG, "sdk init success");
//                    }
//
//                    @Override
//                    public void initFail(int errorCode, String msg) {
//                        Log.i(TAG, "sdk init fail:" + msg);
//                    }
//                });
//            } else {
//                Log.i(TAG, "激活失败");
//            }
//        } else {
//            Log.i(TAG, "授权文件不存在!");
//        }
//    }
//
//    private static  ArrayList<String> list = new ArrayList<>();
//    private static boolean success = false;
//
//    //读取文本文件中的内容
//    public static String readFile(String strFilePath, String mark) {
//        String path = strFilePath;
//        String content = ""; //文件内容字符串
//        //打开文件
//        File file = new File(path);
//        //如果path是传递过来的参数，可以做一个非目录的判断
//        if (file.isDirectory()) {
//            Log.d("TestFile", "The File doesn't not exist.");
//        } else {
//            try {
//                InputStream instream = new FileInputStream(file);
//                if (instream != null) {
//                    InputStreamReader inputreader = new InputStreamReader(instream);
//                    BufferedReader buffreader = new BufferedReader(inputreader);
//                    String line;
//                    //分行读取
//                    while ((line = buffreader.readLine()) != null) {
//                        content = line;
//                        if (mark.equals("liscense")) {
//                            list.add(line);
//                        }
//                    }
//                    instream.close();
//                }
//            } catch (java.io.FileNotFoundException e) {
//                Log.d("TestFile", "The File doesn't not exist.");
//            } catch (IOException e) {
//                Log.d("TestFile", e.getMessage());
//            }
//        }
//        return content;
//    }
//
//    //判断文件是否存在
//    public static boolean fileIsExists(String strFile) {
//        try {
//            File f = new File(strFile);
//            if (!f.exists()) {
//                return false;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//
//        return true;
//    }
//
//}
