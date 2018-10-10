package com.zl.facesdk.baidu.manger;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.license.AndroidLicenser;
import com.zl.facesdk.baidu.util.FileUitls;
import com.zl.facesdk.baidu.util.PreferencesUtil;
import com.zl.facesdk.baidu.util.ZipUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FaceSDKActivate {
    private static final String TAG = "Baidu";

    public static void setActivateKey(String key) {
        if (TextUtils.isEmpty(key)) return;
        PreferencesUtil.putString("activate_key", key);
    }

    public static void activate(Context mContext, SdkActivateListener activateListener) {
        offLineActive(mContext, activateListener);
    }


    public static void offLineActive(Context mContext, SdkActivateListener activateListener) {
        if (FaceSDK.getAuthorityStatus() == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
            activateListener.activateSuccess();
            return;
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String firstPath = path + File.separator + "License.zip";
        if (fileIsExists(firstPath)) {
            if (!TextUtils.isEmpty(firstPath)) {
                ZipUtil.unzip(firstPath);
            }
            if (ZipUtil.isSuccess) {
                String secondPath = path + "/" + "Win.zip";
                if (!TextUtils.isEmpty(secondPath)) {
                    ZipUtil.unzip(secondPath);
                }
            }
            String keyPath = path + "/" + "license.key";
            String key = readFile(keyPath, "key");
            PreferencesUtil.putString("activate_key", key);
            String liscensePaht = path + "/" + "license.ini";
            String liscense = readFile(liscensePaht, "liscense");
            success = FileUitls.c(mContext, FaceSDKManager.LICENSE_NAME, list);
            if (success) {
                Log.i(TAG, "激活成功");
                activateListener.activateSuccess();
            } else {
                Log.i(TAG, "激活失败");
                activateListener.activateFail(-1, "激活失败");
            }
        } else {
            Log.i(TAG, "授权文件不存在!");
            activateListener.activateFail(-2, "授权文件不存在");
        }
    }

    private static ArrayList<String> list = new ArrayList<>();
    private static boolean success = false;

    //读取文本文件中的内容
    public static String readFile(String strFilePath, String mark) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                        if (mark.equals("liscense")) {
                            list.add(line);
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    //判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    public interface SdkActivateListener {
        void activateSuccess();

        void activateFail(int errorCode, String msg);
    }
}
