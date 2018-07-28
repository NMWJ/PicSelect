package com.rx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rx.adapter.RecyclerAdapter;
import com.rx.fragment.SelectDialogFragment;
import com.rx.net.MyRetrofit;
import com.rx.net.ProxyRequestBody;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        MyRetrofit.init();

        checkPermission();

        final ProgressBar progressBar = findViewById(R.id.progress);
        final TextView textView = findViewById(R.id.text);
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

        recyclerView.setAdapter(adapter);

        //上传文件
        findViewById(R.id.putImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MultipartBody.Builder builder = new MultipartBody.Builder();
                for(int i=0;i<list.size();i++){
                    File file = new File(list.get(i));

                    final int j = i;

                    String name = file.getName();

                    String type = name.substring(name.lastIndexOf(".") + 1);

                    RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);

                    ProxyRequestBody proxyRequestBody = new ProxyRequestBody(requestBody, new ProxyRequestBody.UploadListener() {
                        @Override
                        public void onUpload(final double progress) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress((int) progress);
                                    textView.setText("正在上传第"+j+"张图片，已经上传"+(int)progress+"%");
                                }
                            });
                        }
                    });

                    builder.addFormDataPart("image",file.getName(), proxyRequestBody);


                }

                MultipartBody build = builder.build();
                Request request = new Request.Builder()
                        .post(build)
                        .url("http://192.168.3.121:8080/Test/uploadServlet")
                        .build();

                MyRetrofit.getClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_SHORT).show();
                           }
                       });

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
                            switch (type){
                                case 0:
                                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    // Ensure that there's a camera activity to handle the intent
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        // Create the File where the photo should go
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {
                                            // Error occurred while creating the File
                                        }
                                        // Continue only if the File was successfully created
                                        if (photoFile != null) {
                                            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                                    "com.example.android.fileprovider",
                                                    photoFile);
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                        }
                                    }


                                    break;
                                case 1:
                                    Intent intent = new Intent(MainActivity.this,PhotoActivity.class);
                                    intent.putStringArrayListExtra("list",list);
                                    startActivityForResult(intent,100);
                                    break;
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

    private String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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

                Bundle bundle = data.getBundleExtra("bundle");
                ArrayList<String> list = bundle.getStringArrayList("list");
                this.list.clear();
                this.list.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
