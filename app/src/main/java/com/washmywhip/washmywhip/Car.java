package com.washmywhip.washmywhip;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Ross on 2/1/2016.
 */
public class Car {

    private int ownerID;
    private String color;
    private String make;
    private String model;
    private String plate;


    public Car(int ownerID, String color, String make, String model, String plate){
        this.ownerID = ownerID;
        this.color = color;
        this.make = make;
        this.model = model;
        this.plate = plate;
    }

    protected Car(JSONObject jsonData){

        Map<String,Object> carMap = new HashMap<>();
        try {
            carMap = jsonToMap(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Object [] keyList = carMap.keySet().toArray();
        for (Object key:keyList){
            Log.d("makingCAR","key: "+ key.toString()+ "  value: " + carMap.get(key).toString());
        }

        this.ownerID = Integer.parseInt(carMap.get("Owner").toString());
        this.plate = carMap.get("Plate").toString();
        this.model = carMap.get("Model").toString();
        this.make = carMap.get("Make").toString();
        this.color = carMap.get("Color").toString();

        //picpath??

    }


    public int getOwnerID() {
        return ownerID;
    }

    public String getColor() {
        return color;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getPlate() {
        return plate;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
