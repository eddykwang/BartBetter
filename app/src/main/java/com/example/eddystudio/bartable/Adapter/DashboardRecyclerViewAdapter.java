package com.example.eddystudio.bartable.Adapter;

import com.example.eddystudio.bartable.ViewModel.DashboardRecyclerViewItemModel;
import java.util.List;

public class DashboardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

  private List<DashboardRecyclerViewItemModel> itemList;
  private final int layoutId;
  private final int recyclerViewLayout;

  public DashboardRecyclerViewAdapter(List<DashboardRecyclerViewItemModel> itemList,
      int layoutId, int recyclerViewLayout) {
    this.itemList = itemList;
    this.layoutId = layoutId;
    this.recyclerViewLayout = recyclerViewLayout;
  }

  public void setData(DashboardRecyclerViewItemModel item) {
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
