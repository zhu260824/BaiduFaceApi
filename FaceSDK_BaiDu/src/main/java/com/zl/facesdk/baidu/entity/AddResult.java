package com.zl.facesdk.baidu.entity;

import java.io.Serializable;

public class AddResult implements Serializable {
    private static final long serialVersionUID = 6879285812745324092L;
    public static final int SUCCESS = 0;
    public static final int NOT_NULL = 1;
    public static final int NOT_RULE = 2;
    public static final int ERROR_DB = 10;
    private int errorCode;
    private String errorMsg;

    public AddResult(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static AddResult getSuccess() {
        return new AddResult(SUCCESS, "操作成功");
    }

    public static AddResult getErroeDB(String errorMsg) {
        return new AddResult(ERROR_DB, errorMsg);
    }

    public static AddResult getNotNull(String errorMsg) {
        return new AddResult(NOT_NULL, errorMsg);
    }

    public static AddResult getNotRule(String errorMsg) {
        return new AddResult(NOT_RULE, errorMsg);
    }

    public boolean isSuccess() {
        if (this.errorCode == SUCCESS)
            return true;
        return false;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "AddResult{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                '}';
    }
}
