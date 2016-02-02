package com.washmywhip.washmywhip;

/**
 * Created by Ross on 2/1/2016.
 */
import android.util.Log;

import com.stripe.android.*;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

public class PaymentProcessor {


    private PaymentProcessor(Card creditCard, Request request){
        //TODO handle payment processing
        chargeCard(creditCard,request);
        recordTransaction(creditCard,request);
    }


    private void recordTransaction(Card creditCard, Request request) {
        //Store transaction info into database
    }

    private void chargeCard(Card creditCard, Request request) {
        if ( !creditCard.validateCard() ) {
            // Show errors
            // Should catch this when adding a credit card, not at time of payment.
            Log.d("PAYMENT PROCESSING", "This should never print out");
        }


        if(request.getWashType() == 0){
            //charge for basic wash
        } else if(request.getWashType() == 1){
            // chare for standard wash

        } else if(request.getWashType() == 2) {
            //charge for deluxe wash
        }


        Stripe stripe = null;
        try {
            stripe = new Stripe("pk_test_6pRNASCoBOKtIshFeQd4XMUh");

            stripe.createToken(
                    creditCard,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                        }

                        public void onError(Exception error) {
                            // Show localized error message
                            //   Toast.makeText(getContext(),
                            //           error.getLocalizedString(getContext()),
                            //           Toast.LENGTH_LONG
                            //   ).show();
                        }
                    }
            );
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }







}
