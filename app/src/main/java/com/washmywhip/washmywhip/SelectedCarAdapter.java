package com.washmywhip.washmywhip;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ross on 2/3/2016.
 */
public class SelectedCarAdapter extends RecyclerView.Adapter<CarViewHolder> {
    private Context mContext;
    private List<Car> mCars;
    private Car selectedCar;
    private int selectedPosition;
    private SharedPreferences mSharedPreferences;
    private Typeface mFont;


    public SelectedCarAdapter(Context context, ArrayList<Car> cars){
        this.mContext = context;
        this.mCars = cars;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mFont= Typeface.createFromAsset(context.getAssets(), "fonts/Archive.otf");
    }


    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CarViewHolder holder, final int position) {

        final Car car = mCars.get(position);
        holder.carColor.setText(car.getColor());
        holder.carColor.setTypeface(mFont);
        holder.carMake.setText(car.getMake());
        holder.carMake.setTypeface(mFont);
        holder.carModel.setText(car.getModel());
        holder.carModel.setTypeface(mFont);
        holder.carPlate.setText(car.getPlate());
        holder.carPlate.setTypeface(mFont);
        holder.carID = car.getCarID();

        Picasso.with(mContext)
                .load("http://www.WashMyWhip.us/wmwapp/CarImages/car" + holder.carID + "image.jpg")
                .resize(60, 60)
                .centerCrop()
                .into(holder.carPic);

        if(selectedPosition == position){
            holder.itemView.setBackgroundResource(R.drawable.rounded_corner_blue_border);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_corner);
        }
        if(position == 0) {
            selectedPosition = position;
            mSharedPreferences.edit().putInt("SelectedCarID",mCars.get(position).getCarID()).apply();

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("carSelector", "touched: " + position);
                // Updating old as well as new positions
                notifyItemChanged(selectedPosition);
                selectedPosition = position;
                mSharedPreferences.edit().putInt("SelectedCarID",holder.carID).apply();
                notifyItemChanged(selectedPosition);

                // Do your another stuff for your onClick
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCars.size();
    }
    public void add(Car car) {
        mCars.add(car);
        notifyItemInserted(mCars.size() - 1);
    }

    public void remove(int position) {

        mCars.remove(position);
        notifyItemRemoved(position);
    }
    public Car getCar(int position){
        return mCars.get(position);
    }
    public int getSelectedPosition(){
        return selectedPosition;
    }
    public int getSelectedPositionID(){
        return mCars.get(selectedPosition).getCarID();
    }
}
