package com.czbix.v2ex.eventbus.gcm;


import com.czbix.v2ex.event.BaseEvent;

public class DeviceRegisterEvent extends BaseEvent {
    public final boolean isRegister;
    public final boolean isSuccess;

    public DeviceRegisterEvent(boolean isRegister, boolean isSuccess) {
        this.isRegister = isRegister;
        this.isSuccess = isSuccess;
    }
}
