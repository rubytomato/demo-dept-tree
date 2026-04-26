package com.example.samples;

public class Piyo implements PSeries {
  @Override
  public void method1() {
    System.out.println("Piyo#method1");
    Peko peko = new Peko();
    peko.method1();
    method2();
  }
  private void method2() {
    System.out.println("Piyo#method2");
  }
}
