package com.rx.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rx.R;
import com.rx.adapter.RecyclerAdapter;

import java.util.ArrayList;

public class ListDialogFragment extends DialogFragment {

    private Item item;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();

        View view = inflater.inflate(R.layout.activity_photo, container, false);

        if(arguments!=null){
            ArrayList<String> list = arguments.getStringArrayList("list");

            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            RecyclerAdapter<String> recyclerAdapter = new RecyclerAdapter<>(android.R.layout.simple_list_item_1, list, new RecyclerAdapter.BindView<String>() {
                @Override
                public void bindView(RecyclerAdapter.ViewHolder holder, final String obj, int position) {
                    holder.setText(android.R.id.text1,obj);
                    holder.setOnItemClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            item.file(obj);
                            dismiss();
                        }
                    });
                }
            });

            recyclerView.setAdapter(recyclerAdapter);
        }

        return view;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public interface Item{
        void file(String file);
    }

}
