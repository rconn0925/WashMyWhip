package com.washmywhip.washmywhip;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Ross on 2/3/2016.
 */
public class CarAdapter extends RecyclerView.Adapter<CarViewHolder> {
    private Context mContext;
    private List<Car> mCars;
    @Override
    public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.car_item, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarViewHolder holder, int position) {

        final Car car = mCars.get(position);
        holder.carColor.setText(car.getColor());
        holder.carMake.setText(car.getMake());
        holder.carModel.setText(car.getModel());
        holder.carPlate.setText(car.getPlate());
        //holder.carPic.setImageResource(R.drawable.verus);
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
}
