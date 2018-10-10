/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zl.facesdk.baidu.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = -3466242139706108689L;
    private String userId;

    private String userInfo;

    private String groupId;

    private long ctime;

    private long updateTime;

    private List<Feature> featureList = new ArrayList<Feature>();

    public User() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
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

    public List<Feature> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<Feature> featureList) {
        this.featureList = featureList;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userInfo='" + userInfo + '\'' +
                ", groupId='" + groupId + '\'' +
                ", ctime=" + ctime +
                ", updateTime=" + updateTime +
                ", featureList=" + featureList.size() +
                '}';
    }
}
