package com.washmywhip.washmywhip;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ross on 2/18/2016.
 */
public class CardViewHolder extends RecyclerView.ViewHolder{

    @InjectView(R.id.cardBrand)
    public TextView cardBrand;

    @InjectView(R.id.cardNumber)
    public TextView cardNumber;

    @InjectView(R.id.cardExpiration)
    public TextView cardExpiration;

    @InjectView(R.id.cardActive)
    public TextView active;

    private Typeface mFont;
    public CardViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }

}
