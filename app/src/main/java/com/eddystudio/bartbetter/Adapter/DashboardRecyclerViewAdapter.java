package com.eddystudio.bartbetter.Adapter;

import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.R;

import java.util.List;

public class DashboardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

  private List<DashboardRecyclerViewItemVM> itemList;
  private final int layoutId;
  private final int recyclerViewLayout;

  public DashboardRecyclerViewAdapter(List<DashboardRecyclerViewItemVM> itemList,
      int layoutId, int recyclerViewLayout) {
    this.itemList = itemList;
    this.layoutId = layoutId;
    this.recyclerViewLayout = recyclerViewLayout;
  }

  @Override public void onBindViewHolder(ItemHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    holder.itemView.findViewById(R.id.destination)
            .setTransitionName(application.getString(R.string.textTransition) + position);
    holder.itemView.findViewById(R.id.dashboard_from_tv)
            .setTransitionName(application.getString(R.string.textTransition) + position);
  }

  public void modifyData(DashboardRecyclerViewItemVM item, int position){
    this.itemList.set(position, item);
    this.notifyItemChanged(position);
  }

  public void setData(DashboardRecyclerViewItemVM item) {
    this.itemList.add(item);
    this.notifyItemChanged(itemList.size());
  }

  public void deleteData(int position){
    this.itemList.remove(position);
    this.notifyItemRemoved(position);
  }

  public void clearList(){
    this.itemList.clear();
    notifyDataSetChanged();
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
