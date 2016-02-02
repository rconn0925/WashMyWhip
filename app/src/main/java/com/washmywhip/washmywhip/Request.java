package com.washmywhip.washmywhip;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ross on 2/2/2016.
 */
public class Request {

    private User user;
    private Car car;
    private int washType;
    private LatLng destination;

    public Request(User user, Car car, int washType, LatLng destination){
        this.user = user;
        this.car = car;
        this.washType = washType;
        this.destination = destination;
    }

    public int getWashType(){
        return washType;
    }
    public User getUser(){
        return user;
    }
    public Car getCar(){
        return car;
    }
    public LatLng getDestination(){
        return destination;
    }
}
