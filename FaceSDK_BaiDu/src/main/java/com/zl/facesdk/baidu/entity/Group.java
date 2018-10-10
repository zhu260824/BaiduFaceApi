/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.zl.facesdk.baidu.entity;


import java.io.Serializable;

public class Group implements Serializable {
    private static final long serialVersionUID = -2971211624368609684L;
    private String groupId;
    private String desc;

    public Group() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
