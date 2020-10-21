package net.workingdev.popballoons;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity
    implements PopListener {

  ViewGroup contentView;
  private static  String TAG;

  private int[] colors = new int[3];
  private int scrWidth;
  private int scrHeight;
  private int level = 1;

  private TextView levelDisplay;
  private TextView scoreDisplay;
  private int numberOfPins = 5;
  private int pinsUsed;

  private int balloonsLaunched;
  private int balloonsPerLevel = 8;
  private int balloonsPopped = 0;

  private boolean isGameStopped = true;

  private ArrayList<ImageView> pinImages = new ArrayList<>();
  private ArrayList<Balloon> balloons = new ArrayList<>();

  private int userScore;
  Button btn;

  Audio audio;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    TAG = getClass().getName();

    getWindow().setBackgroundDrawableResource(R.mipmap.background);
    setContentView(R.layout.activity_main);

    colors[0] = Color.argb(255, 255, 0, 0);
    colors[1] = Color.argb(255, 0, 255, 0);
    colors[2] = Color.argb(255, 0, 0, 255);

    contentView = (ViewGroup) findViewById(R.id.content_view);
    levelDisplay = (TextView) findViewById(R.id.level_display);
    scoreDisplay = (TextView) findViewById(R.id.score_display);

    pinImages.add((ImageView) findViewById(R.id.pushpin1));
    pinImages.add((ImageView) findViewById(R.id.pushpin2));
    pinImages.add((ImageView) findViewById(R.id.pushpin3));
    pinImages.add((ImageView) findViewById(R.id.pushpin4));
    pinImages.add((ImageView) findViewById(R.id.pushpin5));

    btn = (Button) findViewById(R.id.btn);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startLevel();
      }
    });


    contentView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          setToFullScreen();
        }
        return false;
      }
    });

    audio = new Audio(this);
    audio.prepareMediaPlayer(this);

  }

  @Override
  protected void onResume() {
    super.onResume();

    updateGameStats();
    setToFullScreen();

    ViewTreeObserver viewTreeObserver = contentView.getViewTreeObserver();
    if (viewTreeObserver.isAlive()) {
      viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
          contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          scrWidth = contentView.getWidth();
          scrHeight = contentView.getHeight();
        }
      });
    }

  }

  public void launchBalloon(int xPos) {

    balloonsLaunched++;

    int curColor = colors[nextColor()];
    Balloon btemp = new Balloon(MainActivity.this, curColor, 100,  level);
    btemp.setY(scrHeight);
    btemp.setX(xPos);

    balloons.add(btemp);

    contentView.addView(btemp);
    btemp.release(scrHeight, 5000);

    Log.d(TAG, "Balloon created");

  }

   private void startLevel() {

    if (isGameStopped) {

      isGameStopped = false;
      startGame();
    }

    updateGameStats();
    new LevelLoop(level).start();

  }

  private void finishLevel() {

    Log.d(TAG, "FINISH LEVEL");

    String message = String.format("Level %d finished!", level);
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    level++;
    updateGameStats();
    btn.setText(String.format("Start level %d", level));

    Log.d(TAG, String.format("balloonsLaunched = %d", balloonsLaunched));
    balloonsPopped = 0;

  }

  private void updateGameStats() {
    levelDisplay.setText(String.format("%s", level));
    scoreDisplay.setText(String.format("%s", userScore));
  }

  private void setToFullScreen() {

    contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  private static int nextColor() {

    int max = 2;
    int min = 0;
    int retval = 0;

    Random random = new Random();
    retval = random.nextInt((max - min) + 1) + min;

    Log.d(TAG, String.format("retval = %d", retval));
    return retval;
  }

  @Override
  public void popBalloon(Balloon bal, boolean isTouched) {

    balloonsPopped++;
    balloons.remove(bal);
    contentView.removeView(bal);

    audio.playSound();

    if(isTouched) {
      userScore++;
      scoreDisplay.setText(String.format("%d", userScore));
    }
    else {
      pinsUsed++;
      if (pinsUsed <= pinImages.size() ) {
        pinImages.get(pinsUsed -1).setImageResource(R.drawable.pin_broken);
        Toast.makeText(this, "Ouch!",Toast.LENGTH_SHORT).show();
      }
      if(pinsUsed == numberOfPins) {
        gameOver();
      }
    }

    if (balloonsPopped == balloonsPerLevel) {
      finishLevel();
    }
  }

  private void startGame() {

    // reset the scores
    userScore = 0;
    level = 1;

    updateGameStats();

    //reset the pushpin images
    for (ImageView pin: pinImages) {
      pin.setImageResource(R.drawable.pin);
    }

    audio.playMusic();
  }

  private void gameOver() {

    isGameStopped = true;
    Toast.makeText(this, "Game Over", Toast.LENGTH_LONG).show();
    btn.setText("Play game");

    for (Balloon bal : balloons) {
      bal.setPopped(true);
      contentView.removeView(bal);
    }

    balloons.clear();
    audio.pauseMusic();
  }

  class LevelLoop extends Thread {

    private int shortDelay = 500;
    private int longDelay = 1_500;
    private int maxDelay;
    private int minDelay;
    private int delay;
    private int looplevel;

    int balloonsLaunched = 0;

    public LevelLoop(int argLevel) {
      looplevel = argLevel;
    }

    public void run() {

      while (balloonsLaunched <= balloonsPerLevel) {

        balloonsLaunched++;
        Random random = new Random(new Date().getTime());
        final int xPosition = random.nextInt(scrWidth - 200);

        maxDelay = Math.max(shortDelay, (longDelay - ((looplevel -1)) * 500));
        minDelay = maxDelay / 2;
        delay = random.nextInt(minDelay) + minDelay;

        Log.i(TAG, String.format("Thread delay = %d", delay));

        try {
          Thread.sleep(delay);
        }
        catch(InterruptedException e) {
          Log.e(TAG, e.getMessage());
        }

        // need to wrap this on runOnUiThread

        runOnUiThread(new Thread() {
          public void run() {
            launchBalloon(xPosition);
          }
        });

      }

    }
  }

}
