package net.workingdev.crazy8;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SplashScreen extends View {

  private Bitmap titleG;
  private Bitmap playBtnUp;
  private Bitmap playBtnDn;
  private int scrW;
  private int scrH;
  private boolean playBtnPressed;
  private Context ctx;
  private String TAG = getContext().getClass().getName();

  public SplashScreen(Context context) {
    super(context);

    ctx = context;
    titleG = BitmapFactory.decodeResource(getResources(), R.drawable.splash_graphic);
    playBtnUp = BitmapFactory.decodeResource(getResources(), R.drawable.btn_up);
    playBtnDn = BitmapFactory.decodeResource(getResources(), R.drawable.btn_down);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int titleGLeftPos =  (scrW - titleG.getWidth())/2;
    canvas.drawBitmap(titleG, titleGLeftPos, 100, null);
//    canvas.drawBitmap(titleG, 100, 100, null);
    int playBtnLeftPos = (scrW - playBtnUp.getWidth())/2;
    if (playBtnPressed) {
      canvas.drawBitmap(playBtnDn, playBtnLeftPos, (int)(scrH *0.5), null);
    } else {
      canvas.drawBitmap(playBtnUp, playBtnLeftPos, (int)(scrH *0.5), null);
    }
  }

  @Override
  public void onSizeChanged (int w, int h, int oldw, int oldh){
    super.onSizeChanged(w, h, oldw, oldh);
    scrW = w;
    scrH = h;
    Log.d(TAG, "SCR W: " + scrW);
    Log.d(TAG, "SCR H: " + scrH);
  }

  public boolean onTouchEvent(MotionEvent event) {
    int evtAction = event.getAction();

    int X = (int)event.getX();
    int Y = (int)event.getY();

    switch (evtAction ) {

      case MotionEvent.ACTION_DOWN:

        int btnLeft = (scrW - playBtnUp.getWidth())/2;
        int btnRight = btnLeft + playBtnUp.getWidth();
        int btnTop = (int) (scrH * 0.5);
        int btnBottom = btnTop + playBtnUp.getHeight();

        boolean withinBtnBounds = X > btnLeft && X < btnRight &&
                                  Y > btnTop &&
                                  Y < btnBottom;

        if (withinBtnBounds) {
          playBtnPressed = true;
        }
        break;

      case MotionEvent.ACTION_MOVE:
        break;

      case MotionEvent.ACTION_UP:
        if (playBtnPressed) {
          Intent gameIntent = new Intent(ctx, CrazyEight.class);
          ctx.startActivity(gameIntent);
        }
        playBtnPressed = false;
        break;
    }
    invalidate();
    return true;
  }
}
