package com.example.eddystudio.bartable.UI;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.eddystudio.bartable.MainActivity;
import com.example.eddystudio.bartable.R;
import com.example.eddystudio.bartable.ViewModel.RouteDetailViewModel;
import com.example.eddystudio.bartable.databinding.FragmentRoutDetailBinding;

public class RouteDetailFragment extends Fragment {
  private FragmentRoutDetailBinding binding;
  private String from;
  private String to;
  private int color;
  private String transName;

  public RouteDetailFragment() {
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      Bundle savedInstanceState) {
    binding = FragmentRoutDetailBinding.inflate(inflater, container, false);
    Bundle arg = getArguments();
    if (arg != null) {
      from = arg.getString(MainActivity.BUDDLE_ARG_FROM);
      to = arg.getString(MainActivity.BUDDLE_ARG_TO);
      color = arg.getInt("color");
      transName = arg.getString("transitionName");
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
      binding.linearLayout.setTransitionName(transName);
      binding.textView18.setTransitionName(getString(R.string.textTransition));
    }
    //if (binding.appToolbar !=null)
    //binding.appToolbar.setBackgroundColor(color);
    binding.setVm(new RouteDetailViewModel(from,to, color));
    return binding.getRoot();
  }
}
