package org.javarosa.xform.parse;

import java.util.Random;

class ParkMiller extends Random {
    private static final long MODULUS = 2147483647;
    private static final long MULTIPLIER = 16807;

    private double seed;

    ParkMiller(long seed) {
        this.seed = Long.valueOf(seed).doubleValue() % MODULUS;
        if (this.seed <= 0)
            this.seed += (MODULUS - 1);
    }

    @Override
    protected int next(int bits) {
        seed = seed * MULTIPLIER % MODULUS;
        return Double.valueOf(seed).intValue();
    }

    @Override
    public double nextDouble() {
        return ((double) (this.nextInt() - 1)) / (MODULUS - 1);
    }
}
