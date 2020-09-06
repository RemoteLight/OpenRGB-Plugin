package de.lars.openrgbplugin.utils;

import java.util.ArrayList;
import java.util.List;

public class ValueHolder {

    private String name;
    private String outputId;
    private List<Integer> devices;

    public ValueHolder(String name, String outputId, List<Integer> devices) {
        this.name = name;
        this.outputId = outputId;
        if(devices != null)
            this.devices = devices;
        else
            this.devices = new ArrayList<>();
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

    public List<Integer> getDevices() {
        return devices;
    }

    public void setDevices(List<Integer> devices) {
        this.devices = devices;
    }

    public void addDevice(int deviceId) {
        devices.add(deviceId);
    }

    public void removeDevice(int deviceId) {
        devices.remove(deviceId);
    }
}
