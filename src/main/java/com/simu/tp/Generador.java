package com.simu.tp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

// genera valores para FDP para distribuciones exponenciales
public class Generador {
    private double lambda;
    private Random r = new Random();

    public Generador(double lambda) {
        this.lambda = lambda;
    }

    public int generar() {
        double random = r.nextDouble();
        while (random == 1.0) {
            random = r.nextDouble();
        }

        return (int) Math.floor(-1 / lambda * Math.log(1 - random));
    }
}
