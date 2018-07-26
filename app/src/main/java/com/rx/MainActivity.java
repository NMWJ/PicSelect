package com.rx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.rx.adapter.RecyclerAdapter;
import com.rx.fragment.SelectDialogFragment;

import java.util.ArrayList;

/**
 * retrofit file download
 * 申请权限
 */


public class MainActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private ArrayList<String> list = new ArrayList<>();
    private RecyclerAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter = new RecyclerAdapter<>(R.layout.item_photo, list, new RecyclerAdapter.BindView<String>() {
            @Override
            public void bindView(RecyclerAdapter.ViewHolder holder, final String path, int position) {
                holder.setImageResources(R.id.image,path);
                holder.setVisibility(R.id.checkbox,false);
                holder.setOnItemClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, EditPicActivity.class);
                        intent.putExtra("path", path);
                        startActivity(intent);
                    }
                });
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, permissions[1]) == PackageManager.PERMISSION_GRANTED) {

                    SelectDialogFragment fragment = new SelectDialogFragment();
                    fragment.setAddPhoto(new SelectDialogFragment.AddPhotoListener() {
                        @Override
                        public void addPhotoListener(int type) {
                            if(type==1){
                                Intent intent = new Intent(MainActivity.this,PhotoActivity.class);
                                startActivityForResult(intent,100);
                            }
                        }
                    });
                    fragment.show(getSupportFragmentManager(),"");
                } else {
                    checkPermission();
                }
            }
        });
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, permissions, 100);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"可以开心的使用功能了",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "功能暂时无法使用，请到设置中打开...", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<String> temp = data.getStringArrayListExtra("list");
                temp.clear();
                list.addAll(temp);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
