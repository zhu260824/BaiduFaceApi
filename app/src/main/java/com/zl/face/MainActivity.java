package com.zl.face;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.idl.facesdk.FaceInfo;
import com.zl.face.util.FileUtil;
import com.zl.facesdk.baidu.FaceSDKUtil;
import com.zl.facesdk.baidu.api.FaceApi;
import com.zl.facesdk.baidu.entity.ARGBImg;
import com.zl.facesdk.baidu.entity.Group;
import com.zl.facesdk.baidu.entity.IdentifyRet;
import com.zl.facesdk.baidu.entity.User;
import com.zl.facesdk.baidu.manger.FaceSDKManager;
import com.zl.facesdk.baidu.util.FeatureUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnA, btnD, btnGL, btnGA, btnR, btnS, btnRead, btnUL;
    private TextView tvMsg;
    private List<Group> groupList = new ArrayList<>();
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnA = findViewById(R.id.btn_active);
        btnD = findViewById(R.id.btn_det);
        btnGL = findViewById(R.id.btn_g_list);
        btnGA = findViewById(R.id.btn_g_add);
        btnR = findViewById(R.id.btn_reg);
        btnS = findViewById(R.id.btn_search);
        btnRead = findViewById(R.id.btn_read);
        btnUL = findViewById(R.id.btn_u_list);
        tvMsg = findViewById(R.id.tv_msg);
//        BaiduUtil.init(MainActivity.this);
//        BaiduActive.setBaiduKey("");
        FaceSDKUtil.init(MainActivity.this, "", false, new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.i(TAG, "sdk init start");
            }

            @Override
            public void initSuccess() {
                Log.i(TAG, "sdk init initSuccess");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                Log.i(TAG, "sdk init initFail");
            }
        });
//        btnA.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BaiduActive.offLineActive(v.getContext());
//            }
//        });
        btnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BaiduUtil.detect(v.getContext());
                Bitmap bitmap = FileUtil.getAssetPic(v.getContext(), "zl.png");
                if (null == bitmap) return;
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceInfo[] faceInfos = FaceSDKUtil.detect(argbImg);
            }
        });
        btnUL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<User> userList = FaceApi.getInstance().getUserList(groupId);
                if (null == userList || userList.size() < 1) {
                    showMsg("没有用户");
                } else {
                    StringBuffer stringBuffer = new StringBuffer();
                    for (User user : userList) {
                        stringBuffer.append("userId:");
                        stringBuffer.append(user.getUserId());
                        stringBuffer.append("UserInfo:");
                        stringBuffer.append(user.getUserInfo());
                        stringBuffer.append(";FeatureList:");
                        stringBuffer.append(user.getFeatureList().size());
                        stringBuffer.append("\n");
                    }
                    showMsg(stringBuffer.toString());
                }
            }
        });

        btnGL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupList = FaceApi.getInstance().getGroupList(0, 1000);
                if (null == groupList || groupList.size() < 1) {
                    showMsg("没有分组");
                } else {
                    groupId = groupList.get(0).getGroupId();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Group group : groupList) {
                        stringBuffer.append("groupId:");
                        stringBuffer.append(group.getGroupId());
                        stringBuffer.append(";groupDesc:");
                        stringBuffer.append(group.getDesc());
                        stringBuffer.append("\n");
                    }
                    showMsg(stringBuffer.toString());
                }
            }
        });

        btnGA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, AddGroupActivity.class));
//                BaiduUtil.addGroup("bg_001", "第一个分组");
                FaceSDKUtil.addGroup("bg_001", "第一个分组");
            }
        });
        btnR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, RegActivity.class));

                Bitmap bitmap = FileUtil.getAssetPic(v.getContext(), "zlr.jpg");
                if (null == bitmap) return;
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
//                BaiduUtil.addUser(groupId, "ZL", "zlr", argbImg);
                FaceSDKUtil.addUser(groupId, "ZL", "zlr", argbImg);

            }
        });
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BaiduUtil.loadFeature2Memery(groupId);
                FaceSDKUtil.loadFeature2Memery(groupId);
            }
        });
        btnS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BaiduUtil.detect(v.getContext());
                Bitmap bitmap = FileUtil.getAssetPic(v.getContext(), "zl.png");
                if (null == bitmap) return;
                ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                FaceInfo[] faceInfos = FaceSDKUtil.detect(argbImg);
                if (faceInfos != null && faceInfos.length > 0) {
                    IdentifyRet identifyRet= FaceSDKUtil.identity(argbImg, faceInfos[0]);
                    showMsg(identifyRet==null?"":identifyRet.toString());
                }
            }
        });

    }

    public void showMsg(String msg) {
        String last = tvMsg.getText().toString();
        if (TextUtils.isEmpty(last)) {
            tvMsg.setText(msg);
        } else {
            tvMsg.setText(last + "\n" + msg);
        }
    }


}
