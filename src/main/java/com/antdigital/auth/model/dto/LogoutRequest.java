package com.antdigital.auth.model.dto;

/**
 * 退出登录请求对象。
 */
public class LogoutRequest {

    /** 是否仅退出当前设备 */
    private Boolean currentDeviceOnly;

    public Boolean getCurrentDeviceOnly() {
        return currentDeviceOnly;
    }

    public void setCurrentDeviceOnly(Boolean currentDeviceOnly) {
        this.currentDeviceOnly = currentDeviceOnly;
    }
}
