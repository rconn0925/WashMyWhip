package com.washmywhip.washmywhip;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AddCarFragment extends Fragment implements View.OnClickListener{

    private static final String COLOR = "color";
    private static final String MODEL = "model";
    private static final String MAKE = "make";
    private static final String PLATE = "plate";
    private static final String PIC = "pic";

    private String carMake;
    private String carModel;
    private String carColor;
    private String carPlate;
    private int carPic;

    @InjectView(R.id.addCarPicture)
    ImageView carImage;
    @InjectView(R.id.addCarColor)
    EditText colorEditText;
    @InjectView(R.id.addCarMake)
    EditText makeEditText;
    @InjectView(R.id.addCarModel)
    EditText modelEditText;
    @InjectView(R.id.addCarPlate)
    EditText plateEditText;
    @InjectView(R.id.saveCar)
    Button saveButton;

    private OnFragmentInteractionListener mListener;

    public AddCarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddCarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance() {
        AddCarFragment fragment = new AddCarFragment();
        return fragment;
    }


    //used to update existing car?
    public static Fragment newInstance(String make,String model,String color,String plate, int pic) {
        AddCarFragment fragment = new AddCarFragment();
        Bundle args = new Bundle();
        args.putString(MAKE, make);
        args.putString(MODEL, model);
        args.putString(COLOR, color);
        args.putString(PLATE, plate);
        args.putInt(PIC, pic);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            carMake = getArguments().getString(MAKE);
            carModel = getArguments().getString(MODEL);
            carColor = getArguments().getString(COLOR);
            carPlate = getArguments().getString(PLATE);
            carPic = getArguments().getInt(PIC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_car, container, false);
        ButterKnife.inject(this, v);

        saveButton.setOnClickListener(this);

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == saveButton.getId()) {
            //Pop up succuss or failure

            Fragment profileFragment = ProfileFragment.newInstance();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, profileFragment).commit();

        } else if (v.getId() == carImage.getId()){
            //selectImage();
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //0 is request code
                    startActivityForResult(intent, 0);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), 1);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
