package com.choistec.cms.scannerReg.unit;

public class MonitorItem {
    String mDeviceName;
    String mAddr;
    private String ScannerId;

    public String getmDeviceName() {
        return mDeviceName;
    }

    public void setmDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }


    public String getScannerId() {
        return ScannerId;
    }

    public void setScannerId(String scannerId) {
        ScannerId = scannerId;
    }

    public String getmAddr() {
        return mAddr;
    }

    public void setmAddr(String addr) {
        this.mAddr = addr;
    }
}
