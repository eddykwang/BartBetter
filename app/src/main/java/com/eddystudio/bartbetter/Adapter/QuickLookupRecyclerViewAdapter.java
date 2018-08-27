package com.eddystudio.bartbetter.Adapter;


import com.eddystudio.bartbetter.ViewModel.QuickLookupRecyclerViewItemVM;
import com.eddystudio.bartbetter.R;

import java.util.ArrayList;

public class QuickLookupRecyclerViewAdapter extends BaseRecyclerViewAdapter {
  private ArrayList<QuickLookupRecyclerViewItemVM> bartList = new ArrayList<>();
  private final int layoutId;
  private final int recyclerViewlayout;

  public QuickLookupRecyclerViewAdapter(int layoutId, int recyclerViewlayout) {
    this.layoutId = layoutId;
    this.recyclerViewlayout = recyclerViewlayout;
  }

  public void setData(ArrayList<QuickLookupRecyclerViewItemVM> bartList) {
    this.bartList = bartList;
    this.notifyDataSetChanged();
  }

  public void clearData(){
    this.bartList.clear();
    this.notifyDataSetChanged();
  }

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
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
