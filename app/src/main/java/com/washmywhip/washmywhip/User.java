package com.washmywhip.washmywhip;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Ross on 2/2/2016.
 */
public class User {

    private int userID;
    private String username;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private Date creationDate;

    private User(int userID, String username, String email,String phone,String firstName,String lastName,Date creationDate) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.creationDate = creationDate;
    }
    private User(HashMap jsonData){
        //get user info from dictionary
    }
}
