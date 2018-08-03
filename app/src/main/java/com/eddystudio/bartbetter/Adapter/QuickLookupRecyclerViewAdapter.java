package com.eddystudio.bartbetter.Adapter;


import com.eddystudio.bartbetter.ViewModel.QuickLookupRecyclerViewItemVM;
import com.eddystudio.bartbetter.R;

import java.util.ArrayList;

public class QuickLookupRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    private final ArrayList<QuickLookupRecyclerViewItemVM> bartList;
    private final int layoutId;
    private final int recyclerViewlayout;

    public QuickLookupRecyclerViewAdapter(ArrayList<QuickLookupRecyclerViewItemVM> bartList, int layoutId, int recyclerViewlayout) {
        this.bartList = bartList;
        this.layoutId = layoutId;
        this.recyclerViewlayout = recyclerViewlayout;
    }

    public void setData(ArrayList<QuickLookupRecyclerViewItemVM> bartList){
        this.bartList.clear();
        this.bartList.addAll(bartList);
        this.notifyDataSetChanged();
    }

    @Override public void onBindViewHolder(ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        // the view being shared
        holder.itemView.findViewById(R.id.destination)
            .setTransitionName(application.getString(R.string.textTransition) + position);
    }

    @Override
    protected Object getObjectForPosition(int position) {
        return bartList.get(position);
    }

    @Override
    protected int getLayoutIdForPosition(int position) {
        return layoutId;
    }

    @Override
    protected int getlayoutId() {
        return recyclerViewlayout;
    }

    @Override
    public int getItemCount() {
        return bartList.size();
    }
}
