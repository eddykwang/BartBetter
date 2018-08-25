package com.eddystudio.bartbetter.Adapter;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.eddystudio.bartbetter.Model.Uilt;
import com.eddystudio.bartbetter.R;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.Callback;
import static android.support.v7.widget.helper.ItemTouchHelper.DOWN;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.support.v7.widget.helper.ItemTouchHelper.UP;

public class CardSwipeController extends Callback {

  private SwipeControllerActions action = null;

  private final Context context;

  private final SwipeAction swipeAction;

  public enum SwipeAction {ADD, DELETE}

  public CardSwipeController(Context context, SwipeAction swipeAction) {
    this.context = context;
    this.swipeAction = swipeAction;
  }

  public void setAction(SwipeControllerActions swipteAction) {
    this.action = swipteAction;
  }

  @Override
  public boolean isLongPressDragEnabled() {
    return true;
  }

  @Override
  public boolean isItemViewSwipeEnabled() {
    return true;
  }

  @Override
  public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    if(action != null) {
      action.onDragFinished();
    }

    if(swipeAction == SwipeAction.DELETE) {
      CardView cardView = viewHolder.itemView.findViewById(R.id.dashboard_cardview);
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
      layoutParams.setMargins(Uilt.convertDpToPx(8), Uilt.convertDpToPx(8), Uilt.convertDpToPx(8), Uilt.convertDpToPx(8));
      cardView.requestLayout();
    }

    super.clearView(recyclerView, viewHolder);
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    int dragFlag = swipeAction == SwipeAction.DELETE ? UP | DOWN : 0;
    int swipeDirc = swipeAction == SwipeAction.ADD ? RIGHT : LEFT;
    return makeMovementFlags(dragFlag, swipeDirc);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
    if(action != null)
      action.onDragged(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    return true;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    if(action != null)
      action.onSwiped(viewHolder.getAdapterPosition());
  }

  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    if(actionState == ACTION_STATE_DRAG) {
      CardView cardView = viewHolder.itemView.findViewById(R.id.dashboard_cardview);

      if(action != null) {
        action.onSelected();
      }

      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
      layoutParams.setMargins(Uilt.convertDpToPx(16), Uilt.convertDpToPx(8), Uilt.convertDpToPx(-16), Uilt.convertDpToPx(8));
      cardView.requestLayout();

    }
    super.onSelectedChanged(viewHolder, actionState);
  }

  @Override
  public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    View itemview = viewHolder.itemView;
    int itemHeight = itemview.getBottom() - itemview.getTop();

    Paint clearPaint = new Paint();
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    boolean isCanceled = dX == 0f && !isCurrentlyActive;
    if(isCanceled) {
      c.drawRect(itemview.getRight() + dX, (float) itemview.getTop(), (float) itemview.getRight(), (float) itemview.getBottom(), clearPaint);
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
      return;
    }

    ColorDrawable background = new ColorDrawable();

    if(swipeAction == SwipeAction.ADD) {
      background.setColor(context.getColor(R.color.routColor_green));
      background.setBounds((int) (itemview.getLeft() + dX), itemview.getTop(), itemview.getLeft(), itemview.getBottom());
    } else {
      background.setColor(context.getColor(R.color.tw__composer_red));
      background.setBounds((int) (itemview.getRight() + dX), itemview.getTop(), itemview.getRight(), itemview.getBottom());
    }
    background.draw(c);

    Drawable actionIcon = swipeAction == SwipeAction.ADD ?
        ContextCompat.getDrawable(context, R.drawable.ic_add_black_24dp) :
        ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp);
    int intrinsicWidth = actionIcon.getIntrinsicWidth();
    int intrinsicHeight = actionIcon.getIntrinsicHeight();


    int deleteIconTop = itemview.getTop() + (itemHeight - intrinsicHeight) / 2;
    int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
    int deleteIconBottom = deleteIconTop + intrinsicHeight;

    int deleteIconLeft = swipeAction == SwipeAction.ADD ? itemview.getLeft() + deleteIconMargin : itemview.getRight() - deleteIconMargin - intrinsicWidth;
    int deleteIconRight = swipeAction == SwipeAction.ADD ? itemview.getLeft() + deleteIconMargin + intrinsicWidth : itemview.getRight() - deleteIconMargin;

    actionIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
    actionIcon.draw(c)
    ;
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

  }
}
