package com.eddystudio.bartbetter.Adapter;


public interface SwipeControllerActions {
  void onDragged(int fromPos, int toPos);

  void onSelected();

  void onDragFinished();

  void onSwiped(int position);
}
