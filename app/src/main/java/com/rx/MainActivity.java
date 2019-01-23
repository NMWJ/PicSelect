package com.rx;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rx.adapter.RecyclerAdapter;
import com.rx.entity.Image;
import com.rx.fragment.SelectDialogFragment;
import com.rx.net.MyRetrofit;
import com.rx.net.ProxyRequestBody;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private ArrayList<Image> list = new ArrayList<>();
    private RecyclerAdapter<Image> adapter;
    private String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 101;
    private static final int REQUEST_SELECT_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyRetrofit.init();
        checkPermission();
        final TextView textView = findViewById(R.id.text);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter = new RecyclerAdapter<>(R.layout.item_photo, list, new RecyclerAdapter.BindView<Image>() {
            @Override
            public void bindView(RecyclerAdapter.ViewHolder holder, final Image image, int position) {
                holder.setImageResources(R.id.image,image.getPath());
                holder.setVisibility(R.id.checkbox,false);
                holder.setProgress(R.id.progress,image.getProgress());
                holder.setOnItemClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, EditPicActivity.class);
                        intent.putExtra("path", image.getPath());
                        startActivity(intent);
                    }
                });
            }
        });

        recyclerView.setAdapter(adapter);
        RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if(itemAnimator!=null) ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);


        //上传文件
        findViewById(R.id.putImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.ALTERNATIVE);
                boolean tag = false;
                for(int i=0;i<list.size();i++){
                    final int j = i;
                    final File file = new File(list.get(i).getPath());

                    final String name = file.getName();
                    String type = name.substring(name.lastIndexOf(".") + 1);
                    final RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);
                    builder.addFormDataPart("dis","这是一组图片");
                    ProxyRequestBody proxyRequestBody = new ProxyRequestBody(requestBody, new ProxyRequestBody.UploadListener() {
                        @Override
                        public void onUpload(final double progress) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    list.get(j).setProgress((int) progress);
                                    //progressBar.setProgress((int) progress);
                                    adapter.notifyItemChanged(j);
                                    textView.setText("正在上传第"+(j+1)+"张图片，已经上传"+(int)progress+"%");

                                }
                            });
                        }
                    });

                    builder.addFormDataPart("detail_image",file.getName(),proxyRequestBody);
                    tag = true;

                }

                if(tag){

                    Request request = new Request.Builder()
                            .post(builder.build())
                            .url("http://192.168.3.121:8080/Test/uploadServlet")
                            .build();
                    MyRetrofit.getClient().newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                            if(response.code()==200){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }else {
                    Toast.makeText(MainActivity.this,"你未选择任何图片",Toast.LENGTH_SHORT).show();
                }
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
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        File photoFile = null;
                                        try {
                                            photoFile = createImageFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        // Continue only if the File was successfully created
                                        if (photoFile != null) {
                                            Uri photoURI = FileProvider.getUriForFile(MainActivity.this, "com.rx.photo", photoFile);
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                        }
                                    }

                                    break;
                                case 1:
                                    Intent intent = new Intent(MainActivity.this,PhotoActivity.class);
                                    intent.putParcelableArrayListExtra("list",list);
                                    startActivityForResult(intent,REQUEST_SELECT_PHOTO);
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

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

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
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:

                if(resultCode == RESULT_OK){
                    this.list.add(new Image(false,mCurrentPhotoPath,0));
                    adapter.notifyDataSetChanged();

                    //发布到系统相册
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(mCurrentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                }

                break;
            case REQUEST_SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    if (data != null) {

                        Bundle bundle = data.getBundleExtra("bundle");
                        ArrayList<String> list = bundle.getStringArrayList("list");
                        this.list.clear();
                        if (list != null) for (String path : list) {
                            this.list.add(new Image(false, path, 0));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                break;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
