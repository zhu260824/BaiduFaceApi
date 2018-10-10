package com.baidu.aip.baidu;

import android.text.TextUtils;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Group;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduUtil {

    public static AddResult addGroup(String groupId, String desc) {
        if (TextUtils.isEmpty(groupId)) {
            return AddResult.getNotNull("groupId不能为空");
        }
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z_-]{1,}$");
        Matcher matcher = pattern.matcher(groupId);
        if (!matcher.matches()) {
            return AddResult.getNotRule("groupId由数字、字母、下划线中的一个或者多个组合");
        }
        Group group = new Group();
        group.setGroupId(groupId);
        group.setDesc(desc);
        boolean ret = FaceApi.getInstance().groupAdd(group);
        if (ret) {
            return AddResult.getSuccess();
        } else {
            return AddResult.getErroeDB("数据库添加错误");
        }
    }


}
