package com.simu.tp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Simulador {
    private final static BigInteger HV = new BigInteger("2592000"); // 30 dias en segundos
    private BigInteger t;
    private BigInteger tf;

    // datos
    private BigInteger tpll;
    private List<BigInteger> tps;
    private List<BigInteger> tpr;

    private Generador ia = new Generador(0.01609);
    private Generador ts = new Generador(0.04031);
    private Generador tr = new Generador(0.01572);

    // variables de control
    private BigInteger n;
    private BigInteger m;

    // variables de estado
    private BigInteger ns;
    private BigInteger ne;
    private BigInteger nt;

    // variables resultado
    private List<BigInteger> stoca;
    private List<BigInteger> stoco;

    private List<BigInteger> itoca;
    private List<BigInteger> itoco;

    public Simulador(int n, int m) {
        this(n, m, HV.subtract(BigInteger.ONE));
    }

    public Simulador(int n, int m, BigInteger tf) {
        this.n = BigInteger.valueOf(n);
        this.m = BigInteger.valueOf(m);

        this.tf = tf;
        this.t = BigInteger.ZERO;
        this.tpll = BigInteger.ZERO;

        this.tps = new ArrayList<>(n);
        this.tpr = new ArrayList<>(m);
        this.stoca = new ArrayList<>(n);
        this.stoco = new ArrayList<>(m);
        this.itoca = new ArrayList<>(n);
        this.itoco = new ArrayList<>(m);

        for (int i = 0; i < n; i++) {
            tps.add(HV);
            stoca.add(BigInteger.ZERO);
            itoca.add(t);
        }

        for (int i = 0; i < m; i++) {
            tpr.add(HV);
            stoco.add(BigInteger.ZERO);
            itoco.add(t);
        }

        this.ns = BigInteger.ZERO;
        this.ne = BigInteger.ZERO;
        this.nt = BigInteger.ZERO;

    }

    public void simular() throws Exception {
        while (!terminado()) {
            paso();
        }

        BigDecimal td = new BigDecimal(t);
        System.out.printf("%s clientes atendidos en %s segundos.\n", nt, td);
        for (BigInteger tiempoOcioso : stoca) {
            BigDecimal promedio = new BigDecimal(tiempoOcioso).multiply(BigDecimal.valueOf(100)).divide(td, RoundingMode.DOWN);
            System.out.printf("%s segundos, %s%%\n", tiempoOcioso, promedio);
        }
        System.out.println("----");

        for (BigInteger tiempoOcioso : stoco) {
            BigDecimal promedio = new BigDecimal(tiempoOcioso).multiply(BigDecimal.valueOf(100)).divide(td, RoundingMode.DOWN);
            System.out.printf("%s segundos, %s%%\n", tiempoOcioso, promedio);
        }
    }

    private boolean terminado() {
        return t.compareTo(tf) >= 0;
    }

    private void paso() throws Exception {
        int ir = indicePrimerRetiro();
        int is = indicePrimerSalida();

        if (tpll.compareTo(tps.get(is)) <= 0) {
            if (tpll.compareTo(tpr.get(ir)) <= 0) {
                llegada();
            } else {
                retiro(ir);
            }
        } else if (tps.get(is).compareTo(tpr.get(ir)) <= 0) {
            salida(is);
        } else {
            retiro(ir);
        }
    }

    private void llegada() throws Exception {
        t = tpll;
        BigInteger ia = BigInteger.valueOf(this.ia.generar());
        tpll = t.add(ia);
        ns = ns.add(BigInteger.ONE);

        if (ns.compareTo(n) <= 0) {
            int i = indiceCajaLibre();
            stoca.set(i, stoca.get(i).add(t).subtract(itoca.get(i)));
            BigInteger ta = BigInteger.valueOf(this.ts.generar());
            tps.set(i, t.add(ta));
        }
    }

    private void salida(int is) throws Exception {
        t = tps.get(is);
        ns = ns.subtract(BigInteger.ONE);
        ne = ne.add(BigInteger.ONE);

        // pueden atenderlo en la cocina?
        if (ne.compareTo(m) <= 0) {
            int i = indiceCocinaLibre();
            stoco.set(i, stoco.get(i).add(t).subtract(itoco.get(i)));
            BigInteger tr = BigInteger.valueOf(this.tr.generar());
            tpr.set(i, t.add(tr));
        }

        // puedo seguir atendiendo?
        if (ns.compareTo(n) >= 0) {
            stoca.set(is, stoca.get(is).add(t).subtract(itoca.get(is)));
            BigInteger ts = BigInteger.valueOf(this.ts.generar());
            tps.set(is, t.add(ts));
        } else {
            itoca.set(is, t);
            tps.set(is, HV);
        }
    }

    private void retiro(int ir) {
        t = tpr.get(ir);
        ne = ne.subtract(BigInteger.ONE);
        if (ne.compareTo(m) >= 0) {
            BigInteger tr = BigInteger.valueOf(this.tr.generar());
            tpr.set(ir, t.add(tr));
        } else {
            itoco.set(ir, t);
            tpr.set(ir, HV);
        }

        nt = nt.add(BigInteger.ONE);
    }

    private int indicePrimerSalida() {
        int indiceMenor = 0;
        for (int i = 1; i < n.intValue(); i++) {
            if (tps.get(indiceMenor).compareTo(tps.get(i)) > 0) {
                indiceMenor = i;
            }
        }

        return indiceMenor;
    }

    private int indicePrimerRetiro() {
        int indiceMenor = 0;
        for (int i = 1; i < m.intValue(); i++) {
            if (tpr.get(indiceMenor).compareTo(tpr.get(i)) > 0) {
                indiceMenor = i;
            }
        }

        return indiceMenor;
    }

    private int indiceCajaLibre() throws Exception {
        for (int i = 0; i < n.intValue(); i++) {
            if (tps.get(i).equals(HV)) return i;
        }

        throw new Exception("Se pidió una caja libre cuando no había ninguna disponible.");
    }

    private int indiceCocinaLibre() throws Exception {
        for (int i = 0; i < n.intValue(); i++) {
            if (tpr.get(i).equals(HV)) return i;
        }

        throw new Exception("Se pidió una cocina libre cuando no había ninguna disponible.");

    }
}
