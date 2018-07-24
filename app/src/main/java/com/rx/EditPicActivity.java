package com.rx;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class EditPicActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pic);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");

        ImageView imageView = findViewById(R.id.imageView);
        Glide.with(this).load(path).into(imageView);

    }
}
