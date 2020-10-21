package net.workingdev.crazy8;


import java.util.List;

public class ComputerPlayer {

  public int playCard(List<Card> hand, int suit, int rank) {
    int play = 0;
    for (int i = 0; i < hand.size(); i++) {
      int tempId = hand.get(i).getId();
      int tempRank = hand.get(i).getRank();
      int tempSuit = hand.get(i).getSuit();
      if (tempRank != 8) {
        if (rank == 8) {
          if (suit == tempSuit) {
            play = tempId;
          }
        } else if (suit == tempSuit || rank == tempRank) {
          play = tempId;
        }
      }
    }
    if (play == 0) {
      for (int i = 0; i < hand.size(); i++) {
        int tempId = hand.get(i).getId();
        if (tempId == 108 || tempId == 208 || tempId == 308 || tempId == 408) {
          play = tempId;
        }
      }
    }
    return play;
  }

  public int chooseSuit(List<Card> hand) {
    int suit = 100;
    int numDiamonds = 0;
    int numClubs = 0;
    int numHearts = 0;
    int numSpades = 0;
    for (int i = 0; i < hand.size(); i++) {
      int tempRank = hand.get(i).getRank();
      int tempSuit = hand.get(i).getSuit();
      if (tempRank != 8) {
        if (tempSuit == 100) {
          numDiamonds++;
        } else if (tempSuit == 200) {
          numClubs++;
        } else if (tempSuit == 300) {
          numHearts++;
        } else if (tempSuit == 400) {
          numSpades++;
        }
      }
    }
    if (numClubs > numDiamonds && numClubs > numHearts && numClubs > numSpades) {
      suit = 200;
    } else if (numHearts > numDiamonds && numHearts > numClubs && numHearts > numSpades) {
      suit = 300;
    } else if (numSpades > numDiamonds && numSpades > numClubs && numSpades > numHearts) {
      suit = 400;
    }
    return suit;
  }

}

