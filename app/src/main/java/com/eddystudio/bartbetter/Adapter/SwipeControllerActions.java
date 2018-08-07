package com.eddystudio.bartbetter.Adapter;


public interface SwipeControllerActions {
  void onDragged(int fromPos, int toPos);

  void onDragFinished();

  void onSwiped(int position);
}
