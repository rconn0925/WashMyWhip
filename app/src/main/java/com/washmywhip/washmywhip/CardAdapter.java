package com.washmywhip.washmywhip;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross on 2/18/2016.
 */
public class CardAdapter extends RecyclerView.Adapter<CardViewHolder> {
    private Context mContext;
    private List<Card> mCards;


    public CardAdapter(Context context, ArrayList<Card> cards){
        this.mContext = context;
        this.mCards = cards;
    }


    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {

        final Card card = mCards.get(position);
        holder.cardExpiration.setText(card.getExpiration());
        String CCText = mContext.getResources().getString(R.string.CCBullets) +" "+ card.getLastFour();
        holder.cardNumber.setText(CCText);
        holder.cardBrand.setText(card.getCardType());
        holder.cardExpiration.setText(card.getExpiration());
        if(card.isActive){
            holder.active.setText(R.string.Default);
        } else {
            holder.active.setVisibility(View.INVISIBLE);
        }
      //  holder.carMake.setText(car.getMake());
       // holder.carModel.setText(car.getModel());
       // holder.carPlate.setText(car.getPlate());
    //    holder.carID = car.getOwnerID();
        // holder.carPic.setImageURI(Uri.parse(car.getPic()));
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }
    public void add(Card card) {
        mCards.add(card);
        notifyItemInserted(mCards.size() - 1);
    }

    public void remove(int position) {

        mCards.remove(position);
        notifyItemRemoved(position);
    }
    public Card getCard(int position){
        return mCards.get(position);
    }
    public Card[] getCards(){
        Card[] cardArray = new Card[mCards.size()];
        return mCards.toArray(cardArray);
    }
}
