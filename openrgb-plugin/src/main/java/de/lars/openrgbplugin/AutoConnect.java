package de.lars.openrgbplugin;

import de.lars.openrgbplugin.utils.ClientConnectEvent;
import de.lars.openrgbwrapper.OpenRGB;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AutoConnect {

    /** try to connect interval */
    private int interval;
    /** check timer */
    private final Timer timer;
    /** client instance */
    private final OpenRGB client;
    /** active state */
    private boolean isActive;
    /** timer task instance (could be null) */
    private ConnectTask connectTask;

    public AutoConnect(int interval, OpenRGB client) {
        this.interval = interval;
        this.client = client;
        this.timer = new Timer("OpenRGB Plugin AutoConnect");
    }

    /**
     * Returns whether the timer is currently active and trying to connect to the
     * server in the specified interval.
     * @return      whether the timer is active or not
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the interval in milliseconds. Has no effect if the timer is already running.
     * @param interval  timer interval in milliseconds
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * Gets the currently specified timer interval.
     * @return          timer interval in milliseconds
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Start the timer with the specified interval. Has no effect if timer
     * is already running.
     */
    public void start() {
        if(!isActive) {
            isActive = true;
            runNewTask();
        }
    }

    /**
     * Stop currently running timer.
     */
    public void stop() {
        if(connectTask != null) {
            connectTask.cancel();
            isActive = false;
        }
    }

    /**
     * Schedule a new timer task with the specified interval.
     */
    protected void runNewTask() {
        if(connectTask != null)
            connectTask.cancel();
        connectTask = new ConnectTask();
        timer.schedule(connectTask, getInterval());
    }

    private class ConnectTask extends TimerTask {

        @Override
        public void run() {
            if(client.isConnected()) {
                // client is already connected
                stop();
                return;
            }
            // try to connect
            try {
                boolean connected = client.connect();
                if(connected) {
                    OpenRgbPlugin.print(String.format("AutoConnect: Could connect to %s:%d",
                            client.getClient().getHostname(), client.getClient().getPort()));
                    // stop timer
                    stop();
                    // call connect event
                    OpenRgbPlugin.getInstance().getInterface().getEventHandler().call(new ClientConnectEvent(ClientConnectEvent.Type.CONNECT));
                    return;
                }
            } catch (IOException e) {
                // show no error message
            }
            // run timer again
            runNewTask();
        }
    }

}
