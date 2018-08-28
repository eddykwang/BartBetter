package com.eddystudio.bartbetter.Adapter;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.eddystudio.bartbetter.ViewModel.DashboardRecyclerViewItemVM;
import com.eddystudio.bartbetter.R;

import java.util.Collections;
import java.util.List;

public class DashboardRecyclerViewAdapter extends BaseRecyclerViewAdapter {

  private List<DashboardRecyclerViewItemVM> itemList;
  private final int layoutId;
  private final int recyclerViewLayout;
  private boolean isModifyingData = false;

  public DashboardRecyclerViewAdapter(List<DashboardRecyclerViewItemVM> itemList,
                                      int layoutId, int recyclerViewLayout) {
    this.itemList = itemList;
    this.layoutId = layoutId;
    this.recyclerViewLayout = recyclerViewLayout;
  }

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
    super.onBindViewHolder(holder, position);
    TextView textView = holder.itemView.findViewById(R.id.textView2);
    if(textView.getText().equals("Unavailable")) {
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
      textView.setTypeface(Typeface.DEFAULT);
    } else {
      textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
      textView.setTypeface(Typeface.DEFAULT_BOLD);
    }
    holder.itemView.findViewById(R.id.destination)
        .setTransitionName(application.getString(R.string.text_to_transition) + position);
    holder.itemView.findViewById(R.id.dashboard_from_tv)
        .setTransitionName(application.getString(R.string.text_from_transition) + position);

    if(isModifyingData) {
      holder.itemView.setAnimation(AnimationUtils.loadLayoutAnimation(application, R.anim.layout_animation_fall_down).getAnimation());
    }
  }

  public void modifyData(DashboardRecyclerViewItemVM item) {
    for(int i = 0; i < itemList.size(); ++i) {
      if(itemList.get(i).getFrom().equals(item.getFrom()) && itemList.get(i).getTo().equals(item.getTo())) {
        itemList.set(i, item);
        isModifyingData = true;
        this.notifyItemChanged(i);
      }
    }
  }

  public void modifyData(DashboardRecyclerViewItemVM item, int position) {
    this.itemList.set(position, item);
    this.notifyItemChanged(position);
  }

  public void addDataInPos(DashboardRecyclerViewItemVM item, int pos) {
    this.itemList.add(pos, item);
    this.notifyItemInserted(pos);
  }

  public void setData(DashboardRecyclerViewItemVM item) {
    this.itemList.add(item);
    this.notifyItemChanged(itemList.size());
  }

  public void deleteData(int position) {
    this.itemList.remove(position);
    this.notifyItemRemoved(position);
  }

  public void swapDataPos(int fromPos, int toPos) {
    if(fromPos < toPos) {
      for(int i = fromPos; i < toPos; i++) {
        Collections.swap(itemList, i, i + 1);
      }
    } else {
      for(int i = fromPos; i > toPos; i--) {
        Collections.swap(itemList, i, i - 1);
      }
    }
    notifyItemMoved(fromPos, toPos);
  }

  public DashboardRecyclerViewItemVM getItemInPos(int pos) {
    return itemList.get(pos);
  }


  public List<DashboardRecyclerViewItemVM> getItemList() {
    return itemList;
  }

  public void clearList() {
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
