package com.eddystudio.bartbetter.Adapter;

import android.view.animation.AnimationUtils;

import com.eddystudio.bartbetter.R;
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

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    holder.itemView.setAnimation(AnimationUtils.loadLayoutAnimation(application, R.anim.layout_animation_fall_down).getAnimation());
  }

  public void addData(RouteDetailRecyclerViewModel item) {
    routeInfoListList.add(item);
    this.notifyItemChanged(routeInfoListList.size());
  }

  public void clearAllData() {
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
