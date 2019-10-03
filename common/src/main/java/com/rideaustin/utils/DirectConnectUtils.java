package com.rideaustin.utils;

public class DirectConnectUtils {

  private static final String START_ID = "10000";
  private static final String ALPHABET = "0123456789";

  private DirectConnectUtils(){}

  public static String generateNextId() {
    return generateNextId(START_ID);
  }

  public static String generateNextId(String last) {
    char[] alphabet = new char[ALPHABET.length()];
    char[] nextId = new char[last.length()];
    ALPHABET.getChars(0, ALPHABET.length(), alphabet, 0);
    last.getChars(0, last.length(), nextId, 0);

    int position = nextId.length - 1;
    increaseAtPosition(alphabet, nextId, position);

    return new String(nextId);
  }

  private static void increaseAtPosition(char[] alphabet, char[] nextId, int position) {
    if (position < 0) {
      throw new IllegalArgumentException("Alphabet is exhausted!");
    }
    char lastPlace = nextId[position];
    int next = getNextAlphabetPosition(alphabet, lastPlace);


    if (next != alphabet.length) {
      nextId[position] = alphabet[next];
    } else {
      nextId[position] = alphabet[0];
      increaseAtPosition(alphabet, nextId, position - 1);
    }
  }

  private static int getNextAlphabetPosition(char[] alphabet, char lastPlace) {
    int next = 0;
    while (lastPlace != alphabet[next++]){
      //do nothing
    }
    return next;
  }
}
