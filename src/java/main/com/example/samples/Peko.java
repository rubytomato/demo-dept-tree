package com.example.samples;

public class Peko implements PSeries {
  @Override
  public void method1() {
    System.out.println("Peko#method1");
    Taco taco = new Taco();
    taco.method2();
  }
}
