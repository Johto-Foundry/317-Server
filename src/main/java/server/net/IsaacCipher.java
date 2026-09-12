package server.net;

public final class IsaacCipher {

    private static final int SIZE = 256;
    private static final int GOLDEN_RATIO = 0x9e3779b9;

    private final int[] results = new int[SIZE];
    private final int[] memory = new int[SIZE];

    private int count;
    private int accumulator;
    private int lastResult;
    private int counter;

    public IsaacCipher(int[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed");
        }
        if (seed.length > SIZE) {
            throw new IllegalArgumentException("seed is too large");
        }

        System.arraycopy(seed, 0, results, 0, seed.length);
        initialize();
    }

    public int nextInt() {
        if (count == 0) {
            generate();
            count = SIZE;
        }
        return results[--count];
    }

    private void initialize() {
        int a = GOLDEN_RATIO;
        int b = GOLDEN_RATIO;
        int c = GOLDEN_RATIO;
        int d = GOLDEN_RATIO;
        int e = GOLDEN_RATIO;
        int f = GOLDEN_RATIO;
        int g = GOLDEN_RATIO;
        int h = GOLDEN_RATIO;

        for (int round = 0; round < 4; round++) {
            a ^= b << 11;
            d += a;
            b += c;
            b ^= c >>> 2;
            e += b;
            c += d;
            c ^= d << 8;
            f += c;
            d += e;
            d ^= e >>> 16;
            g += d;
            e += f;
            e ^= f << 10;
            h += e;
            f += g;
            f ^= g >>> 4;
            a += f;
            g += h;
            g ^= h << 8;
            b += g;
            h += a;
            h ^= a >>> 9;
            c += h;
            a += b;
        }

        for (int index = 0; index < SIZE; index += 8) {
            a += results[index];
            b += results[index + 1];
            c += results[index + 2];
            d += results[index + 3];
            e += results[index + 4];
            f += results[index + 5];
            g += results[index + 6];
            h += results[index + 7];

            a ^= b << 11;
            d += a;
            b += c;
            b ^= c >>> 2;
            e += b;
            c += d;
            c ^= d << 8;
            f += c;
            d += e;
            d ^= e >>> 16;
            g += d;
            e += f;
            e ^= f << 10;
            h += e;
            f += g;
            f ^= g >>> 4;
            a += f;
            g += h;
            g ^= h << 8;
            b += g;
            h += a;
            h ^= a >>> 9;
            c += h;
            a += b;

            memory[index] = a;
            memory[index + 1] = b;
            memory[index + 2] = c;
            memory[index + 3] = d;
            memory[index + 4] = e;
            memory[index + 5] = f;
            memory[index + 6] = g;
            memory[index + 7] = h;
        }

        for (int index = 0; index < SIZE; index += 8) {
            a += memory[index];
            b += memory[index + 1];
            c += memory[index + 2];
            d += memory[index + 3];
            e += memory[index + 4];
            f += memory[index + 5];
            g += memory[index + 6];
            h += memory[index + 7];

            a ^= b << 11;
            d += a;
            b += c;
            b ^= c >>> 2;
            e += b;
            c += d;
            c ^= d << 8;
            f += c;
            d += e;
            d ^= e >>> 16;
            g += d;
            e += f;
            e ^= f << 10;
            h += e;
            f += g;
            f ^= g >>> 4;
            a += f;
            g += h;
            g ^= h << 8;
            b += g;
            h += a;
            h ^= a >>> 9;
            c += h;
            a += b;

            memory[index] = a;
            memory[index + 1] = b;
            memory[index + 2] = c;
            memory[index + 3] = d;
            memory[index + 4] = e;
            memory[index + 5] = f;
            memory[index + 6] = g;
            memory[index + 7] = h;
        }

        generate();
        count = SIZE;
    }

    private void generate() {
        lastResult += ++counter;

        for (int index = 0; index < SIZE; index++) {
            int value = memory[index];

            switch (index & 3) {
                case 0 -> accumulator ^= accumulator << 13;
                case 1 -> accumulator ^= accumulator >>> 6;
                case 2 -> accumulator ^= accumulator << 2;
                case 3 -> accumulator ^= accumulator >>> 16;
            }

            accumulator += memory[(index + 128) & 0xff];
            int result = memory[index] = memory[(value >>> 2) & 0xff] + accumulator + lastResult;
            results[index] = lastResult = memory[(result >>> 10) & 0xff] + value;
        }
    }
}
