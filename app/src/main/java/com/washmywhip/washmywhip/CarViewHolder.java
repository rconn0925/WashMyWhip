package com.washmywhip.washmywhip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Type;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ross on 2/3/2016.
 */
public class CarViewHolder extends RecyclerView.ViewHolder{

    @InjectView(R.id.carMakeProfile)
    public TextView carMake;

    @InjectView(R.id.carModelProfile)
    public TextView carModel;


    @InjectView(R.id.carColorProfile)
    public TextView carColor;

    @InjectView(R.id.carePlateProfile)
    public TextView carPlate;

    @InjectView(R.id.carPictureProfile)
    public CircleImageView carPic;

    int carID;

    public CarViewHolder(View itemView) {
        super(itemView);
        ButterKnife.inject(this, itemView);

    }

}
