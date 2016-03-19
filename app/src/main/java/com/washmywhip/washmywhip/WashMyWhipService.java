package com.washmywhip.washmywhip;

import com.stripe.android.model.Token;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by Ross on 2/8/2016.
 */
public interface WashMyWhipService {


    @FormUrlEncoded
    @POST("/createUser.php")
    void createUser (@Field("username") String username,@Field("password")  String password,@Field("email")  String email,@Field("firstname")  String firstName,@Field("lastname") String lastName,@Field("phone")  String phone, Callback<Integer> callback);

    //1 if successful, 0 if sql error, "Email Exists" if email already exists in the table
    @FormUrlEncoded
    @POST("/updateUserInfo.php")
    void updateUserInfo (@Field("userID") int userId,@Field("email") String email, @Field("firstName") String firstName,@Field("lastName")  String lastName, @Field("phone") String phoneNumber, Callback<Object> callback);


    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/updateUserPassword.php")
    void updateUserPassword (@Field("username") String username, @Field("password") String password,Callback<JSONObject> callback);

    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/getUserWithID.php")
    void getUserWithID (@Field("userID") int userID, Callback<Object> callback);



    @FormUrlEncoded
    @POST("/requestTemporaryPassword.php")
    void requestTemporaryPassword (@Field("email") String email, Callback<JSONObject> callback);
    /*
    [User info] - information about specified user in JSON format. The first JSON element is success:1 to represent a successful login. The second element is isTempPass, which again is a boolean that represents if the user has just reset their password or not.
	Unsuccessful: [integer] - if there is more than 1 user, or no users with the specified username.
     */
    @FormUrlEncoded
    @POST("/requestUserLogin.php")
    void requestUserLogin(@Field("email") String email,@Field("password") String password, Callback<JSONObject> callback);




    @FormUrlEncoded
    @POST("/createCar.php")
    void createCar(@Field("userID") int userID,@Field("color") String color,
                   @Field("make") String make,@Field("model") String model,
                   @Field("plate") String plate, @Field("hasImage") boolean hasImage,
                   Callback<String> callback);

    @FormUrlEncoded
    @POST("/updateCar.php")
    void updateCar(@Field("userID") int userID,@Field("color") String color,
                   @Field("make") String make,@Field("model") String model,
                   @Field("plate") String plate, @Field("hasImage") boolean hasImage,
                   Callback<JSONObject> callback);


    @FormUrlEncoded
    @POST("/deleteCar.php")
    void deleteCar(@Field("carID") int carID, Callback<Object> callback);

    @FormUrlEncoded
    @POST("/getCars.php")
    void getCars(@Field("userID") int userID, Callback<List<JSONObject>> callback);

    @FormUrlEncoded
    @POST("/getStripeCustomer.php")
    void getStripeCustomer(@Field("userID")int userID, Callback<JSONObject> callback);

    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/addPaymentSource.php")
    void addPaymentSource (@Field("userID") int userID,@Field("stripeToken") String tokenID, Callback<Object> callback);

    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/changeDefaultStripeCard.php")
    void changeDefaultStripeCard (@Field("userID") int userID,@Field("defaultID") String cardID, Callback<Object> callback);

    //1 if successful, 0 if sql error
    @FormUrlEncoded
    @POST("/deleteStripeCard.php")
    void deleteStripeCard (@Field("userID") int userID,@Field("cardID") String cardID, Callback<Object> callback);

    @Multipart
    @POST("/")
    void uploadCarImage(@Part("file") TypedFile file, Callback<Object> callback);
}
