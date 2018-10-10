/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zl.facesdk.baidu.entity;


import java.io.Serializable;

public class IdentifyRet implements Serializable {
    private static final long serialVersionUID = -3255179955908096519L;
    private String userId;
    private float score;

    public IdentifyRet() {
    }

    public IdentifyRet(String userId, float score) {
        this.userId = userId;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "IdentifyRet{" +
                "userId='" + userId + '\'' +
                ", score=" + score +
                '}';
    }
}
