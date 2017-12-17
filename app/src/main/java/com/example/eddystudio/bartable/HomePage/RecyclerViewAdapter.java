package com.example.eddystudio.bartable.HomePage;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.application.Application;
import com.example.eddystudio.bartable.databinding.SingleRecyclerViewItemBinding;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemHolder> {

    private  final ArrayList<MainRecyclerViewModel> bartModelArrayList;
    private static int lastPosition = 0;

    public RecyclerViewAdapter(ArrayList<MainRecyclerViewModel> bartModelArrayList) {
        this.bartModelArrayList = bartModelArrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View singleItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_recycler_view_item, parent, false);
        return new ItemHolder(singleItemView) ;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.getBinding().setViewModel(bartModelArrayList.get(position));

        Animation animation = AnimationUtils.loadAnimation(Application.appComponet.injectAppContext(),
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        holder.itemView.setOnClickListener((view)->{
            Log.d("recyclerView",position + " clicked");
            Toast.makeText(Application.appComponet.injectAppContext(),"postion: "+ position,Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onViewDetachedFromWindow(ItemHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return bartModelArrayList.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder{

        private static SingleRecyclerViewItemBinding singleRecyclerViewItemBinding;

        public ItemHolder(View itemView) {
            super(itemView);
            singleRecyclerViewItemBinding = DataBindingUtil.bind(itemView);
        }

        public SingleRecyclerViewItemBinding getBinding(){
            return singleRecyclerViewItemBinding;
        }
    }
}
