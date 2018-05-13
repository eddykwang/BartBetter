package com.eddystudio.bartbetter.Adapter;

import com.eddystudio.bartbetter.ViewModel.RouteDetailRecyclerViewModel;

import java.util.ArrayList;

public class RouteDetailRecyclerViewAdapter extends BaseRecyclerViewAdapter {

    private final ArrayList<RouteDetailRecyclerViewModel> routeInfoListList;
    private final int layoutId;
    private final int recyclerViewlayout;

    public RouteDetailRecyclerViewAdapter(ArrayList<RouteDetailRecyclerViewModel> routeInfoList, int layoutId, int recyclerViewlayout) {
        this.routeInfoListList = routeInfoList;
        this.layoutId = layoutId;
        this.recyclerViewlayout = recyclerViewlayout;
    }

    public void addData(RouteDetailRecyclerViewModel item){
        routeInfoListList.add(item);
        this.notifyItemChanged(routeInfoListList.size());
    }

    public void clearAllData(){
        routeInfoListList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    protected Object getObjectForPosition(int position) {
        return routeInfoListList.get(position);
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
        return routeInfoListList.size();
    }
}
