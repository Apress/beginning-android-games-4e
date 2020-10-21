package net.workingdev.crazy8;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CrazyEightView extends View {

  private int scrW;
  private int scrH;
  private Context ctx;
  private List<Card> deck = new ArrayList<Card>();
  private int scaledCW;
  private int scaledCH;
  private Paint paint;
  private List<Card> playerHand = new ArrayList<Card>();
  private List<Card> computerHand = new ArrayList<Card>();
  private int myScore = 0;
  private int computerScore = 0;
  private float scale;
  private Bitmap cardBack;
  private List<Card> discardPile = new ArrayList<Card>();
  private boolean myTurn;
  private ComputerPlayer computerPlayer = new ComputerPlayer();
  private int movingIdx = -1;
  private int movingX;
  private int movingY;
  private int validRank = 8;
  private int validSuit = 0;
  private Bitmap nextCardBtn;
  private int currScore = 0;

  public CrazyEightView(Context context) {
    super(context);
    ctx = context;
    scale = ctx.getResources().getDisplayMetrics().density;
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setColor(Color.BLACK);
    paint.setStyle(Paint.Style.FILL);
    paint.setTextAlign(Paint.Align.LEFT);
    paint.setTextSize(scale*15);
  }

  @Override
  public void onSizeChanged (int w, int h, int oldw, int oldh){
    super.onSizeChanged(w, h, oldw, oldh);
    scrW = w;
    scrH = h;
    Bitmap tempBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.card_back);
    scaledCW = (int) (scrW /8);
    scaledCH = (int) (scaledCW *1.28);
    cardBack = Bitmap.createScaledBitmap(tempBitmap, scaledCW, scaledCH, false);
    nextCardBtn = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_next);
    initializeDeck();
    dealCards();
    drawCard(discardPile);
    validSuit = discardPile.get(0).getSuit();
    validRank = discardPile.get(0).getRank();
    myTurn = new Random().nextBoolean();
    if (!myTurn) {
      computerPlay();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawText("Opponent Score: " + Integer.toString(computerScore), 10, paint.getTextSize()+10, paint);
    canvas.drawText("My Score: " + Integer.toString(myScore), 10, scrH - paint.getTextSize()-10, paint);
    for (int i = 0; i < computerHand.size(); i++) {
      canvas.drawBitmap(cardBack,
          i*(scale*5),
          paint.getTextSize()+(50*scale),
          null);
    }
    float cbackLeft = (scrW/2) - cardBack.getWidth() - 10;
    float cbackTop = (scrH/2) - (cardBack.getHeight() / 2);

    canvas.drawBitmap(cardBack, cbackLeft, cbackTop, null);

    if (!discardPile.isEmpty()) {
      canvas.drawBitmap(discardPile.get(0).getBitmap(),
          (scrW /2)+10,
          (scrH /2)-(cardBack.getHeight()/2),
          null);
    }
    if (playerHand.size() > 7) {
      canvas.drawBitmap(nextCardBtn,
          scrW - nextCardBtn.getWidth()-(30*scale),
          scrH - nextCardBtn.getHeight()- scaledCH -(90*scale),
          null);
    }
    for (int i = 0; i < playerHand.size(); i++) {
      if (i == movingIdx) {
        canvas.drawBitmap(playerHand.get(i).getBitmap(),
            movingX,
            movingY,
            null);
      } else {
        if (i < 7) {
          canvas.drawBitmap(playerHand.get(i).getBitmap(),
              i*(scaledCW +5),
              scrH - scaledCH - paint.getTextSize()-(50*scale),
              null);
        }
      }
    }
    invalidate();
    setToFullScreen();
  }

  public boolean onTouchEvent(MotionEvent event) {
    int eventaction = event.getAction();
    int X = (int)event.getX();
    int Y = (int)event.getY();

    switch (eventaction ) {

      case MotionEvent.ACTION_DOWN:
        if (myTurn) {
          for (int i = 0; i < 7; i++) {
            if (X > i*(scaledCW +5) && X < i*(scaledCW +5) + scaledCW &&
                Y > scrH - scaledCH - paint.getTextSize()-(50*scale)) {
              movingIdx = i;
              movingX = X-(int)(30*scale);
              movingY = Y-(int)(70*scale);
            }
          }
        }
        break;

      case MotionEvent.ACTION_MOVE:
        movingX = X-(int)(30*scale);
        movingY = Y-(int)(70*scale);
        break;

      case MotionEvent.ACTION_UP:
        if (movingIdx > -1 &&
            X > (scrW /2)-(100*scale) &&
            X < (scrW /2)+(100*scale) &&
            Y > (scrH /2)-(100*scale) &&
            Y < (scrH /2)+(100*scale) &&
            (playerHand.get(movingIdx).getRank() == 8 ||
                playerHand.get(movingIdx).getRank() == validRank ||
                playerHand.get(movingIdx).getSuit() == validSuit)) {
          validRank = playerHand.get(movingIdx).getRank();
          validSuit = playerHand.get(movingIdx).getSuit();
          discardPile.add(0, playerHand.get(movingIdx));
          playerHand.remove(movingIdx);
          if (playerHand.isEmpty()) {
            endHand();
          } else {
            if (validRank == 8) {
              changeSuit();
            } else {
              myTurn = false;
              computerPlay();
            }
          }
        }
        if (movingIdx == -1 && myTurn &&
            X > (scrW /2)-(100*scale) &&
            X < (scrW /2)+(100*scale) &&
            Y > (scrH /2)-(100*scale) &&
            Y < (scrH /2)+(100*scale)) {
          if (isValidDraw()) {
            drawCard(playerHand);
          } else {
            Toast.makeText(ctx, "You have a valid play.", Toast.LENGTH_SHORT).show();
          }
        }
        if (playerHand.size() > 7 &&
            X > scrW - nextCardBtn.getWidth()-(30*scale) &&
            Y > scrH - nextCardBtn.getHeight()- scaledCH -(90*scale) &&
            Y < scrH - nextCardBtn.getHeight()- scaledCH -(60*scale)) {
          Collections.rotate(playerHand, 1);
        }
        movingIdx = -1;
        break;
    }
    invalidate();
    return true;
  }

  //////////////////////////////////
  // GAME METHODS STARTS HERE
  /////////////////////////////////

  private void changeSuit() {
    final Dialog changeSuitDlg = new Dialog(ctx);
    changeSuitDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
    changeSuitDlg.setContentView(R.layout.choose_suit_dialog);
    final Spinner spinner = (Spinner) changeSuitDlg.findViewById(R.id.suitSpinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        ctx, R.array.suits, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    Button okButton = (Button) changeSuitDlg.findViewById(R.id.okButton);
    okButton.setOnClickListener(new View.OnClickListener(){
      public void onClick(View view){
        validSuit = (spinner.getSelectedItemPosition()+1)*100;
        String suitText = "";
        if (validSuit == 100) {
          suitText = "Diamonds";
        } else if (validSuit == 200) {
          suitText = "Clubs";
        } else if (validSuit == 300) {
          suitText = "Hearts";
        } else if (validSuit == 400) {
          suitText = "Spades";
        }
        changeSuitDlg.dismiss();
        Toast.makeText(ctx, "You chose " + suitText, Toast.LENGTH_SHORT).show();
        myTurn = false;
        computerPlay();
      }
    });
    changeSuitDlg.show();
  }

  private void initializeDeck() {
    for (int i = 0; i < 4; i++) {
      for (int j = 102; j < 115; j++) {
        int tempId = j + (i*100);
        Card tempCard = new Card(tempId);
        int resourceId = getResources().
            getIdentifier("card" + tempId, "drawable", ctx.getPackageName());
        Bitmap tempBitmap = BitmapFactory.decodeResource(ctx.getResources(), resourceId);
        scaledCW = (int) (scrW /8);
        scaledCH = (int) (scaledCW *1.28);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCW, scaledCH, false);
        tempCard.setBitmap(scaledBitmap);
        deck.add(tempCard);
      }
    }
  }

  private void drawCard(List<Card> hand) {
    hand.add(0, deck.get(0));
    deck.remove(0);
    if (deck.isEmpty()) {
      for (int i = discardPile.size()-1; i > 0 ; i--) {
        deck.add(discardPile.get(i));
        discardPile.remove(i);
        Collections.shuffle(deck,new Random());
      }
    }
  }

  private void dealCards() {
    Collections.shuffle(deck,new Random());
    for (int i = 0; i < 7; i++) {
      drawCard(playerHand);
      drawCard(computerHand);
    }
  }

  private boolean isValidDraw() {
    boolean canDraw = true;
    for (int i = 0; i < playerHand.size(); i++) {
      int tempId = playerHand.get(i).getId();
      int tempRank = playerHand.get(i).getRank();
      int tempSuit = playerHand.get(i).getSuit();
      if (validSuit == tempSuit || validRank == tempRank ||
          tempId == 108 || tempId == 208 || tempId == 308 || tempId == 408) {
        canDraw = false;
      }
    }
    return canDraw;
  }

  private void computerPlay() {
    int tempPlay = 0;
    while (tempPlay == 0) {
      tempPlay = computerPlayer.playCard(computerHand, validSuit, validRank);
      if (tempPlay == 0) {
        drawCard(computerHand);
      }
    }
    if (tempPlay == 108 || tempPlay == 208 || tempPlay == 308 || tempPlay == 408) {
      validRank = 8;
      validSuit = computerPlayer.chooseSuit(computerHand);
      String suitText = "";
      if (validSuit == 100) {
        suitText = "Diamonds";
      } else if (validSuit == 200) {
        suitText = "Clubs";
      } else if (validSuit == 300) {
        suitText = "Hearts";
      } else if (validSuit == 400) {
        suitText = "Spades";
      }
      Toast.makeText(ctx, "Computer chose " + suitText, Toast.LENGTH_SHORT).show();
    } else {
      validSuit = Math.round((tempPlay/100) * 100);
      validRank = tempPlay - validSuit;
    }
    for (int i = 0; i < computerHand.size(); i++) {
      Card tempCard = computerHand.get(i);
      if (tempPlay == tempCard.getId()) {
        discardPile.add(0, computerHand.get(i));
        computerHand.remove(i);
      }
    }
    if (computerHand.isEmpty()) {
      endHand();
    }
    myTurn = true;
  }

  private void endHand() {
    String endHandMsg = "";
    final Dialog endHandDlg = new Dialog(ctx);
    endHandDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
    endHandDlg.setContentView(R.layout.end_hand_dialog);
    updateScores();
    TextView endHandText = (TextView) endHandDlg.findViewById(R.id.endHandText);

    if (playerHand.isEmpty()) {
      if (myScore >= 300) {
        endHandMsg = String.format("You won. You have %d points. Play again?", myScore);
      } else {
        endHandMsg = String.format("You lost, you only got %d", currScore);
      }
    } else if (computerHand.isEmpty()) {
      if (computerScore >= 300) {
        endHandMsg = String.format("Opponent scored %d. You lost. Play again?", computerScore);
      } else {
        endHandMsg = String.format("Opponent has lost. He scored %d points.", currScore);
      }
      endHandText.setText(endHandMsg);
    }

    Button nextHandBtn = (Button) endHandDlg.findViewById(R.id.nextHandButton);

    if (computerScore >= 300 || myScore >= 300) {
      nextHandBtn.setText("New Game");
    }
    nextHandBtn.setOnClickListener(new View.OnClickListener(){
      public void onClick(View view){
        if (computerScore >= 300 || myScore >= 300) {
          myScore = 0;
          computerScore = 0;
        }
        initNewHand();
        endHandDlg.dismiss();
      }
    });
    endHandDlg.show();
  }

  private void updateScores() {
    for (int i = 0; i < playerHand.size(); i++) {
      computerScore += playerHand.get(i).getScoreValue();
      currScore += playerHand.get(i).getScoreValue();
    }
    for (int i = 0; i < computerHand.size(); i++) {
      myScore += computerHand.get(i).getScoreValue();
      currScore += computerHand.get(i).getScoreValue();
    }
  }

  private void initNewHand() {
    currScore = 0;
    if (playerHand.isEmpty()) {
      myTurn = true;
    } else if (computerHand.isEmpty()) {
      myTurn = false;
    }
    deck.addAll(discardPile);
    deck.addAll(playerHand);
    deck.addAll(computerHand);
    discardPile.clear();
    playerHand.clear();
    computerHand.clear();
    dealCards();
    drawCard(discardPile);
    validSuit = discardPile.get(0).getSuit();
    validRank = discardPile.get(0).getRank();
    if (!myTurn) {
      computerPlay();
    }
  }

  private void setToFullScreen() {
    setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    Log.d(getClass().getName(), "setToFullScreen called");
  }
}
