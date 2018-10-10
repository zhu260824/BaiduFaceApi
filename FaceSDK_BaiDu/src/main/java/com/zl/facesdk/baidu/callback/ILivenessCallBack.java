/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zl.facesdk.baidu.callback;


import com.zl.facesdk.baidu.entity.LivenessModel;

public interface ILivenessCallBack {

    public void onCallback(LivenessModel livenessModel);

    public void onTip(int code, String msg);

    public void onCanvasRectCallback(LivenessModel livenessModel);
}
