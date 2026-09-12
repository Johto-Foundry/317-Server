package server.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IsaacCipherTest {

    @Test
    void sameSeedProducesSameSequence() {
        int[] seed = {1, 2, 3, 4};
        IsaacCipher first = new IsaacCipher(seed);
        IsaacCipher second = new IsaacCipher(seed);

        for (int index = 0; index < 1024; index++) {
            assertEquals(first.nextInt(), second.nextInt());
        }
    }

    @Test
    void differentSeedProducesDifferentSequence() {
        IsaacCipher first = new IsaacCipher(new int[]{1, 2, 3, 4});
        IsaacCipher second = new IsaacCipher(new int[]{51, 52, 53, 54});

        assertNotEquals(first.nextInt(), second.nextInt());
    }
}
