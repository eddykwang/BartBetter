package com.example.eddystudio.bartable.Adapter;


import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.ViewModel.HomePageRecyclerViewItemModel;
import java.util.ArrayList;

public class HomePageRecyclerViewAdapter extends BaseRecyclerViewAdapter {
    private final ArrayList<HomePageRecyclerViewItemModel> bartList;
    private final int layoutId;
    private final int recyclerViewlayout;

    public HomePageRecyclerViewAdapter(ArrayList<HomePageRecyclerViewItemModel> bartList, int layoutId, int recyclerViewlayout) {
        this.bartList = bartList;
        this.layoutId = layoutId;
        this.recyclerViewlayout = recyclerViewlayout;
    }

    public void setData(ArrayList<HomePageRecyclerViewItemModel> bartList){
        this.bartList.clear();
        this.bartList.addAll(bartList);
        this.notifyDataSetChanged();
    }

    @Override public void onBindViewHolder(ItemHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        // the view being shared
        holder.itemView.findViewById(R.id.boder_image_view)
            .setTransitionName(application.getString(R.string.goToDetailTransition) + position);
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
