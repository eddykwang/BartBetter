package com.example.eddystudio.bartable.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.ViewModel.RouteDetailViewModel;
import com.example.eddystudio.bartable.databinding.FragmentRoutDetailBinding;

public class RouteDetailFragment extends Fragment {
  private FragmentRoutDetailBinding binding;
  private String from;
  private String to;
  private int color;

  public RouteDetailFragment() {
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentRoutDetailBinding.inflate(inflater, container, false);

    ImageView imageView = getActivity().findViewById(R.id.toolbar_imageView);
    CollapsingToolbarLayout collapsingToolbarLayout = getActivity().findViewById(R.id.toolbar_layout);
    AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar);

    Bundle arg = getArguments();
    if (arg != null) {
      from = arg.getString(MainActivity.BUDDLE_ARG_FROM);
      to = arg.getString(MainActivity.BUDDLE_ARG_TO);
      color = arg.getInt("color");
    }
    imageView.setTransitionName(getString(R.string.goToDetailTransition));

    setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
    //binding.linearLayout.setTransitionName(transName);
    binding.textView18.setTransitionName(getString(R.string.textTransition));
    //if (binding.appToolbar !=null)
    //binding.appToolbar.setBackgroundColor(color);
    binding.setVm(new RouteDetailViewModel(from,to, color));


    imageView.setVisibility(View.VISIBLE);
    imageView.setBackgroundColor(color);
    collapsingToolbarLayout.setTitleEnabled(true);
    appBarLayout.setExpanded(true,true);
    collapsingToolbarLayout.setTitle(from + " -> " +to);
    collapsingToolbarLayout.setPadding(0,0,0,0);
    return binding.getRoot();
  }
}
