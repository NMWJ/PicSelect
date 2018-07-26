package com.rx.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {

    private int layoutId;
    private List<T> data;
    private BindView<T> bindView;
    private ArrayList<T> tempList;
    private ArrayFilter mFilter;

    public RecyclerAdapter(int layoutId, List<T> data, BindView<T> bindView) {
        this.layoutId = layoutId;
        this.data = data;
        this.bindView = bindView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        bindView.bindView(holder,data.get(position),position);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private View itemView;

        private ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

        }

        public void setText(int id,CharSequence sequence){
            TextView textView = itemView.findViewById(id);
            textView.setText(sequence);
        }

        public void setChildViewOnClick(int id, View.OnClickListener onClickListener){
            itemView.findViewById(id).setOnClickListener(onClickListener);
        }

        public void setOnItemClick(View.OnClickListener onClickListener){
            itemView.setOnClickListener(onClickListener);
        }

        public void setImageResources(int id,String path){
            ImageView imageView = itemView.findViewById(id);
            Glide.with(itemView).load(path).into(imageView);
        }

        public void setVisibility(int id,boolean isVisibility){
            View view = itemView.findViewById(id);
            if(isVisibility){
                view.setVisibility(View.VISIBLE);
            }else {
                view.setVisibility(View.GONE);
            }
        }

        public void checked(int id, boolean check, CompoundButton.OnCheckedChangeListener clickListener){
            CheckBox checkBox = itemView.findViewById(id);
            checkBox.setOnCheckedChangeListener(clickListener);
            checkBox.setChecked(check);
        }

    }

    public interface BindView<T>{
        void bindView(ViewHolder holder, T obj, int position);
    }

    private final Object lock = new Object();

    private class ArrayFilter extends Filter {
        //执行刷选
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();//过滤的结果
            //原始数据备份为空时，上锁，同步复制原始数据
            if (tempList == null) {
                synchronized (lock) {
                    tempList = new ArrayList<>(data);
                }
            }
            //当首字母为空时
            if (prefix == null || prefix.length() == 0) {
                ArrayList<T> list;
                synchronized (lock) {//同步复制一个原始备份数据
                    list = new ArrayList<>(tempList);
                }
                results.values = list;
                results.count = list.size();//此时返回的results就是原始的数据，不进行过滤
            } else {
                String prefixString = prefix.toString().toLowerCase();//转化为小写

                ArrayList<T> values;
                synchronized (lock) {//同步复制一个原始备份数据
                    values = new ArrayList<>(tempList);
                }
                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    T t = values.get(i);//从List<User>中拿到User对象
                    final String valueText =t.toString().toLowerCase();//User对象的name属性作为过滤的参数
                    if (valueText.startsWith(prefixString) || valueText.contains(prefixString)) {//第一个字符是否匹配
                        newValues.add(t);//将这个item加入到数组对象中
                    } else {//处理首字符是空格
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {//一旦找到匹配的就break，跳出for循环
                                newValues.add(t);
                                break;
                            }
                        }
                    }
                }
                results.values = newValues;//此时的results就是过滤后的List<User>数组
                results.count = newValues.size();
            }
            return results;
        }

        //刷选结果
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence prefix, FilterResults results) {
            data = (List<T>) results.values;//此时，Adapter数据源就是过滤后的Results
            notifyDataSetChanged();//这个相当于从mDatas中删除了一些数据，只是数据的变化，故使用notifyDataSetChanged()
        }
    }

}
