package com.washmywhip.washmywhip;

import java.util.HashMap;

/**
 * Created by Ross on 2/1/2016.
 */
public class Car {

    private int carID;
    private int ownerID;
    private String color;
    private String make;
    private String model;
    private String plate;


    private Car(int carID, int ownerID, String color, String make,String model, String plate){
        this.carID = carID;
        this.ownerID = ownerID;
        this.color = color;
        this.make = make;
        this.model = model;
        this.plate = plate;
    }

    private Car(HashMap jsonData){

    }
}
