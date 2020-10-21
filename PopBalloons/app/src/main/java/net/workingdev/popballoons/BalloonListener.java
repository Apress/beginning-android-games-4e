package net.workingdev.popballoons;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.util.Log;


public class BalloonListener implements
    Animator.AnimatorListener,
    ValueAnimator.AnimatorUpdateListener{

  Balloon balloon;
  private final String TAG = getClass().getName();

  public BalloonListener(Balloon balloon) {
    this.balloon = balloon;
  }


  @Override
  public void onAnimationUpdate(ValueAnimator valueAnimator) {
    if(!balloon.isPopped()) {
      balloon.setY((float) valueAnimator.getAnimatedValue());
    }
  }

  @Override
  public void onAnimationStart(Animator animator) {

  }

  @Override
  public void onAnimationEnd(Animator animator) {

    if(!balloon.isPopped()) {
      balloon.pop(false);
    }

    Log.d(TAG, "Animation end");
  }

  @Override
  public void onAnimationCancel(Animator animator) {

  }

  @Override
  public void onAnimationRepeat(Animator animator) {

  }

}
