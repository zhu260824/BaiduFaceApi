/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zl.facesdk.baidu.entity;

import android.util.Base64;

import java.io.Serializable;
import java.util.Arrays;

public class Feature implements Serializable {
    private static final long serialVersionUID = -2365004809203627374L;
    private String faceToken;

    private byte[] feature;

    private String userId;

    private String groupId;

    private long ctime;

    private long updateTime;

    private String imageName;

    public Feature() {
    }

    public String getFaceToken() {
        if (feature != null) {
            byte[] base = Base64.encode(feature, Base64.NO_WRAP);
            faceToken = new String(base);
        }
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public byte[] getFeature() {
        return feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "faceToken='" + faceToken + '\'' +
                ", feature=" + Arrays.toString(feature) +
                ", userId='" + userId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", ctime=" + ctime +
                ", updateTime=" + updateTime +
                ", imageName='" + imageName + '\'' +
                '}';
    }
}
