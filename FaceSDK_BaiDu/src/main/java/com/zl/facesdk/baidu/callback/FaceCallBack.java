package com.zl.facesdk.baidu.callback;

public interface FaceCallBack<T> {

    void onSuccess(T t);

    void onFial(int code, int msg);
}
