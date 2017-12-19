package com.example.eddystudio.bartable.Dashboard;


import com.example.eddystudio.bartable.Uilts.BaseRecyclerViewAdapter;

import java.util.ArrayList;

public class DashboardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    private final ArrayList<DashboardRecyclerViewItemModel> itemList;
    private final int layoutId;
    private final int recyclerViewLayout;

    public DashboardRecyclerViewAdapter(ArrayList<DashboardRecyclerViewItemModel> itemList, int layoutId, int recyclerViewLayout) {
        this.itemList = itemList;
        this.layoutId = layoutId;
        this.recyclerViewLayout = recyclerViewLayout;
    }

    @Override
    protected Object getObjectForPosition(int position) {
        return itemList.get(position);
    }

    @Override
    protected int getLayoutIdForPosition(int position) {
        return layoutId;
    }

    @Override
    protected int getlayoutId() {
        return recyclerViewLayout;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
