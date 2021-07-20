package com.choistec.cms.scannerReg;

/**
 * 디바이스 정보 클래스 멤버 변수
 */
public class DeviceItem {

    private String ScannerId = ""; //스캐너 아이디
    private String ScannerMac = ""; //스캐너 맥주소
    private String WifiSsid = "";  //스캐너 와이파이 이름
    private String WifiPW = "";  //스캐너 와이파이 비밀번호
    private String MqttIp = "";  //mqtt 아이피 주소
    private boolean isConnected = false; //연결 상태 체크 변수

    public String getScannerId() {
        return ScannerId;
    }

    public void setScannerId(String scannerId) {
        ScannerId = scannerId;
    }

    public String getScannerMac() {
        return ScannerMac;
    }

    public void setScannerMac(String scannerMac) {
        ScannerMac = scannerMac;
    }

    public String getWifiSsid() {
        return WifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        WifiSsid = wifiSsid;
    }

    public String getWifiPW() {
        return WifiPW;
    }

    public void setWifiPW(String wifiPW) {
        WifiPW = wifiPW;
    }

    public String getMqttIp() {
        return MqttIp;
    }

    public void setMqttIp(String mqttIp) {
        MqttIp = mqttIp;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
