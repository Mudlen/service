package org.example.service;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceData {
    private String data;
    private Timer timer;
    private long ttl;

    public ServiceData(String data, long ttl){
        this.ttl = ttl * 1000;
        this.data = data;
        timer = new Timer();
        timer.schedule(new ServiceDataTask(), this.ttl);
    }

    public String getData() {
        return data;
    }

    public long getTtl(){
        return ttl;
    }

    private class ServiceDataTask extends TimerTask{

        @Override
        public void run() {
            data = null;
            timer.cancel();
        }
    }


}