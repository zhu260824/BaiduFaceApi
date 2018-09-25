package com.zl.face;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.face.FaceCropper;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.aip.utils.ZipUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.license.AndroidLicenser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnA, btnD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnA = findViewById(R.id.btn_active);
        btnD = findViewById(R.id.btn_det);
        PreferencesUtil.initPrefs(this);
        DBManager.getInstance().init(this);
        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offLineActive("/storage/emulated/0");
            }
        });
        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                det();
            }
        });
    }

    private void det() {
        Bitmap bitmap = getAssetPic(this, "zl.png");
        if (null == bitmap) return;

        ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
        FaceSDKManager.getInstance().getFaceDetector().detect(argbImg.data, argbImg.width, argbImg.height);
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetector().getTrackedFaces();
        if (faceInfos != null && faceInfos.length > 0) {
            for (FaceInfo faceInfo : faceInfos) {
                Log.i(TAG, "mWidth:"+faceInfo.mWidth+"mConf:"+faceInfo.mConf
                        +"mCenter_y:"+faceInfo.mCenter_y+"mCenter_x:"+faceInfo.mCenter_x+
                        "mAngle:"+faceInfo.mAngle+"is_live_mouth:"+faceInfo.is_live_mouth()+
                        "is_live_head_up:"+faceInfo.is_live_head_up()+"is_live_head_turn_right:"+faceInfo.is_live_head_turn_right()+
                        "is_live_head_turn_left:"+faceInfo.is_live_head_turn_left()+"is_live_head_down:"+faceInfo.is_live_head_down()+
                        "is_live:"+faceInfo.is_live()+"rightEyeState:"+faceInfo.get_rightEyeState()+
                        "mouthState:"+faceInfo.get_mouthState()+"leftEyeState:"+faceInfo.get_leftEyeState());
                Bitmap sbitmap = FaceCropper.getFace(argbImg.data, faceInfo, argbImg.width);
                saveBitmapFile(sbitmap);
                Rect faceRect= FaceCropper.getFaceRect(argbImg.data, faceInfo, argbImg.width);
                Log.i(TAG,"faceRect：" + faceRect.toString());
                float rgbScore = FaceLiveness.getInstance().rgbLiveness(argbImg.data, argbImg.width, argbImg.height, faceInfo.landmarks);
                Log.i(TAG,"RGB活体得分：" + rgbScore);
            }

        }
        FaceSDKManager.getInstance().getFaceDetector().clearTrackedFaces();
    }


    public static Bitmap getAssetPic(Context mContext, String fileName) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mContext.getAssets().open(fileName));
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveBitmapFile(Bitmap bitmap) {
        String path =  Environment.getExternalStorageDirectory()
                + File.separator
                + "com.zl.face"
                + File.separator
                + "pic.jpg";
        File dir = new File(path).getParentFile();
        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSDK() {
        FaceSDKManager.getInstance().init(this);
        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.i(TAG, "sdk init start");
            }

            @Override
            public void initSuccess() {
                Log.i(TAG, "sdk init success");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                Log.i(TAG, "sdk init fail:" + msg);
            }
        });
    }

    private void offLineActive(String path) {
        if (FaceSDK.getAuthorityStatus() == AndroidLicenser.ErrorCode.SUCCESS.ordinal()) {
            Toast.makeText(this, "已经激活成功", Toast.LENGTH_LONG).show();
            return;
        }

        String firstPath = path + "/" + "License.zip";
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
            success = FileUitls.c(this, FaceSDKManager.LICENSE_NAME, list);
            if (success) {
                Log.i(TAG, "激活成功");
                FaceSDKManager.initStatus = FaceSDKManager.SDK_UNINIT;
                FaceSDKManager.getInstance().init(this);
                FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
                    @Override
                    public void initStart() {
                        Log.i(TAG, "sdk init start");
                    }

                    @Override
                    public void initSuccess() {
                        Log.i(TAG, "sdk init success");
                    }

                    @Override
                    public void initFail(int errorCode, String msg) {
                        Log.i(TAG, "sdk init fail:" + msg);
                    }
                });
            } else {
                Log.i(TAG, "激活失败");
            }
        } else {
            Log.i(TAG, "授权文件不存在!");
        }
    }

    ArrayList<String> list = new ArrayList<>();
    private boolean success = false;

    //读取文本文件中的内容
    public String readFile(String strFilePath, String mark) {
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
    public boolean fileIsExists(String strFile) {
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
}
