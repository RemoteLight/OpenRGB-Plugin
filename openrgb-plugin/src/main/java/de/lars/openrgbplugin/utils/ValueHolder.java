package de.lars.openrgbplugin.utils;

public class ValueHolder {

    private String name;
    private String outputId;
    private String orgbIp;
    private int orgbPort;
    private int deviceId;

    public ValueHolder(String name, String outputId, String orgbIp, int orgbPort, int deviceId) {
        this.name = name;
        this.outputId = outputId;
        this.orgbIp = orgbIp;
        this.orgbPort = orgbPort;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public String getOrgbIp() {
        return orgbIp;
    }

    public void setOrgbIp(String orgbIp) {
        this.orgbIp = orgbIp;
    }

    public int getOrgbPort() {
        return orgbPort;
    }

    public void setOrgbPort(int orgbPort) {
        this.orgbPort = orgbPort;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
}
