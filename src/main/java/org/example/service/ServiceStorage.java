package org.example.service;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ServiceStorage {
    private HashMap<String, ServiceData> serviceStorage;
    private long checkTime;

    public ServiceStorage(long defaultCheckTime){
        serviceStorage = new HashMap<>();
        this.checkTime = TimeUnit.MINUTES.toMillis(defaultCheckTime);
        Timer timer = new Timer();
        timer.schedule(new EmptyDataValidationTask(),checkTime);
    }

    private class EmptyDataValidationTask extends TimerTask{
        @Override
        public void run() {
            serviceStorage.entrySet().removeIf(e -> e.getValue().getData() == null);
        }
    }
    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public void addData(String key, String data, long ttl){
        ServiceData serviceData = new ServiceData(data,ttl);
        serviceStorage.put(key,serviceData);
    }

    public String getData(String key){
        if(serviceStorage.containsKey(key)) {
            return serviceStorage.get(key).getData();
        }
        return null;
    }

    public String remove(String key){
        String deletedData;
        if(serviceStorage.containsKey(key)){
            deletedData = serviceStorage.get(key).getData();
            serviceStorage.remove(key);
            return deletedData;
        }
        return null;
    }

    public String toJson(){
        JSONObject result = new JSONObject();
        for(Map.Entry<String, ServiceData> obj: serviceStorage.entrySet()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", obj.getValue().getData());
            jsonObject.put("ttl",obj.getValue().getTtl());
            result.put(obj.getKey(),jsonObject);
        }
        return result.toJSONString();
    }

    public static ServiceStorage fromJsonToServiceStorage(String json){
        ServiceStorage serviceStorage = new ServiceStorage(5);
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);
        Object[] keys = jsonObject.keySet().toArray();
        for (Object key : keys) {
            JSONObject serviceDataJson = (JSONObject) jsonObject.get(key);
            String data = (String) serviceDataJson.get("data");
            long ttl = (long) serviceDataJson.get("ttl");
            serviceStorage.addData(key.toString(),data,ttl);
        }
        return serviceStorage;
    }

    public void createDumpFile(){
        try {
            File uploadDir = new File("C:\\Users\\Public\\service");
            if(uploadDir.exists()) {
                PrintWriter printWriter = new PrintWriter(uploadDir.getAbsolutePath()+"\\ServiceStorage.txt", "UTF-8");
                printWriter.println(this.toJson());
                printWriter.close();
            }else {
                uploadDir.mkdir();
                PrintWriter printWriter = new PrintWriter(uploadDir.getAbsolutePath()+"\\ServiceStorage.txt", "UTF-8");
                printWriter.println(this.toJson());
                printWriter.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
