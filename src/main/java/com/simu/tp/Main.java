package com.simu.tp;

public class Main {
    public static void main(String[] args) {
        try {
            new Simulador(2, 2).simular();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
