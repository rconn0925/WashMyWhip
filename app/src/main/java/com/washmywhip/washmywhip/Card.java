package com.washmywhip.washmywhip;

/**
 * Created by Ross on 2/18/2016.
 */
public class Card {

    String lastFour;
    String cardType;
    String expiration;
    boolean isActive;
    String id;

    protected Card(String id,String cardType, String lastFour,String expiration, boolean isActive){

        this.id = id;
        this.cardType = cardType;
        this.lastFour = lastFour;
        this.expiration = expiration;
        this.isActive = isActive;

    }
    public String getLastFour() {
        return lastFour;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiration() {
        return expiration;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getId() {return id;}

    public String toString() {
        return cardType + " "+ lastFour + " " + expiration;
    }

    public void setActive(boolean isActive){
        this.isActive = isActive;
    }

}
