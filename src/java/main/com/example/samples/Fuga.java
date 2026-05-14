package com.example.samples;

import com.example.other.exclude.Moge;

public class Fuga {
  public void method1(Long a) throws PException {
    System.out.println("Fuga#method1: " + a);
    Piyo piyo = new Piyo();
    piyo.method1();
    if (a == null) {
      throw new PException("a is null");
    }
  }
  public void method2(String b) {
    System.out.println("Fuga#method2: " + b);
    Moge moge = new Moge();
    moge.method1();
  }
}
