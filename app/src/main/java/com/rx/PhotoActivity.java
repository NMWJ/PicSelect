package com.rx;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rx.adapter.RecyclerAdapter;
import com.rx.entity.Image;
import com.rx.fragment.ListDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PhotoActivity extends AppCompatActivity {

    private HashMap<String, List<String>> map = new HashMap<>();
    private ArrayList<String> list = new ArrayList<>();
    private List<Image> images = new ArrayList<>();
    private RecyclerAdapter<Image> adapter;
    private ListDialogFragment listDialogFragment = new ListDialogFragment();
    private ArrayList<String> pathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final TextView textView = findViewById(R.id.submitPhoto);

        final Intent intent = getIntent();
        ArrayList<Image> list = intent.getParcelableArrayListExtra("list");
        for (Image image:list){
            pathList.add(image.getPath());
        }

        textView.setText("已选择"+"("+String.valueOf(pathList.size())+")");

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent.putStringArrayListExtra("list",pathList);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("list",pathList);
                intent.putExtra("bundle",bundle);
                setResult(RESULT_OK,intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });

        findViewById(R.id.selectPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PhotoActivity.this.list.size() > 0) {
                    listDialogFragment.show(getSupportFragmentManager(), "list");
                    listDialogFragment.setItem(new ListDialogFragment.Item() {
                        @Override
                        public void file(String file) {
                            images.clear();
                            List<String> list = map.get(file);
                            for (String path : list) {
                                Image image;
                                if(pathList.contains(path)){
                                    image = new Image(true, path);
                                }else {
                                    image = new Image(false, path);
                                }
                                images.add(image);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        adapter = new RecyclerAdapter<>(R.layout.item_photo, images, new RecyclerAdapter.BindView<Image>() {
            @Override
            public void bindView(RecyclerAdapter.ViewHolder holder, final Image obj, final int position) {
                holder.setImageResources(R.id.image, obj.getPath());
                holder.setVisibility(R.id.progress,false);

                if(pathList.size()<5){
                    holder.checked(R.id.checkbox, obj.isCheck(), new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            images.get(position).setCheck(b);
                            if(b){
                                if(!pathList.contains(obj.getPath())){
                                    pathList.add(obj.getPath());
                                    textView.setText("已选择"+"("+String.valueOf(pathList.size())+")");
                                }

                            }else {
                                if(pathList.contains(obj.getPath())){
                                    pathList.remove(obj.getPath());
                                    textView.setText("已选择"+"("+String.valueOf(pathList.size())+")");
                                }
                            }
                        }
                    });
                }

                holder.setOnItemClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(PhotoActivity.this, EditPicActivity.class);
                        intent.putExtra("path", obj.getPath());
                        startActivity(intent);
                    }
                });
            }
        });

        recyclerView.setAdapter(adapter);
        initImages();
    }


    // 扫描手机图片
    private void initImages() {

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {

                ContentResolver contentResolver = getContentResolver();
                String[] IMAGE_PROJECTION = {
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID};

                Cursor query = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED + " DESC");

//                Cursor query = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  IMAGE_PROJECTION,
//                        null, null,  IMAGE_PROJECTION[0] + " DESC");

                if (query == null) {
                    Toast.makeText(PhotoActivity.this, "获取相册失败", Toast.LENGTH_SHORT).show();
                } else {

                    while (query.moveToNext()) {
                        //获取单张图片路径
                        String path = query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA));
                        //获取图片所在目录
                        String pathParent = new File(path).getParentFile().getName();
                        if (!map.containsKey(pathParent)) {
                            List<String> chileList = new ArrayList<>();
                            chileList.add(path);
                            map.put(pathParent, chileList);
                        } else {
                            map.get(pathParent).add(path);
                        }

                    }

                    query.close();
                    list.clear();
                    list.addAll(map.keySet());
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("list", list);
                    listDialogFragment.setArguments(bundle);

                    List<String> tempList;
                    if (map.containsKey("Camera")) {
                        tempList = map.get("Camera");
                    } else {
                        tempList = map.get(list.get(0));
                    }

                    for (String path : tempList) {
                        Image image;
                        if(pathList.contains(path)){
                            image = new Image(true, path);
                        }else {
                            image = new Image(false, path);
                        }
                        images.add(image);
                    }

                    emitter.onNext(1);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                if (integer == 1) adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
