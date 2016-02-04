package com.washmywhip.washmywhip;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ross on 2/3/2016.
 */
public class CarViewHolder extends RecyclerView.ViewHolder {

    @InjectView(R.id.carMakeProfile)
    public TextView carMake;

    @InjectView(R.id.carModelProfile)
    public TextView carModel;


    @InjectView(R.id.carColorProfile)
    public TextView carColor;

    @InjectView(R.id.carePlateProfile)
    public TextView carPlate;

    @InjectView(R.id.carPictureProfile)
    public ImageView carPic;


    public CarViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);
    }
}
