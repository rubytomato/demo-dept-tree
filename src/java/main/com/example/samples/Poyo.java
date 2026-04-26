package com.example.samples;

import com.example.other.samples.Popo;

public class Poyo {
  private void method1() {
    System.out.println("Poyo#method1");
  }
  public void method3(String b) {
    System.out.println("Poyo#method3: " + b);
    method1();
    Taco taco = new Taco();
    taco.method1();
    Popo popo = new Popo();
    popo.method1();
  }
}
