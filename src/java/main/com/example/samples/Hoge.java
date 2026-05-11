package com.example.samples;

import java.util.Date;

public class Hoge extends Hogo {

    public void method1(String a, String b) {
        System.out.println("Hoge#method1: " + a + b);

        try {
            Fuga fuga = new Fuga();
            fuga.method1(100L);
            fuga.method2(a + b);
        } catch (PException e) {
            System.out.println("Hoge#method1: " + e.getMessage());
            method4();
        }

        method3();

        Poyo poyo = new Poyo();
        poyo.method3(a + b);
    }

    void method2(Date date) {
        System.out.println("Hoge#method2: " + date);
    }

    private void method4() {
        System.out.println("Hoge#method4");
    }

}
