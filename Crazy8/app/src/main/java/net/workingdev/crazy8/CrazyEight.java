package net.workingdev.crazy8;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class CrazyEight extends AppCompatActivity {

  private View gameView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    gameView = new CrazyEightView(this);
    gameView.setKeepScreenOn(true);
    setContentView(gameView);

  }

  private void setToFullScreen() {
    gameView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setToFullScreen();
  }
}