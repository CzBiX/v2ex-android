package com.czbix.v2ex.eventbus.gcm;

public class DeviceRegisterEvent {
    public final boolean isRegister;
    public final boolean isSuccess;

    public DeviceRegisterEvent(boolean isRegister, boolean isSuccess) {
        this.isRegister = isRegister;
        this.isSuccess = isSuccess;
    }
}
