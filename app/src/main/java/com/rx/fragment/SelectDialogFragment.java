package com.rx.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rx.MainActivity;
import com.rx.PhotoActivity;
import com.rx.R;

public class SelectDialogFragment extends DialogFragment{

    private AddPhotoListener addPhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select, container, false);
        view.findViewById(R.id.takePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"正在开发中...",Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.selectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addPhoto.addPhotoListener(1);
               dismiss();
            }
        });
        return view;
    }

    public void setAddPhoto(AddPhotoListener addPhoto) {
        this.addPhoto = addPhoto;
    }

    public interface AddPhotoListener{
        void addPhotoListener(int type);
    }
}
