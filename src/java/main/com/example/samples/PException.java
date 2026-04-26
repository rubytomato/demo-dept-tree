package com.example.samples;

public class PException extends Exception {
  public PException(String message) {
    super(message);
    method1();
  }

  private void method1() {
    System.out.println("PException#method1");
  }

}
