package server.world.npc;

import org.junit.jupiter.api.Test;
import server.world.map.Tile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcListTest {

    private static final Tile TEST_TILE = new Tile(3200, 3200);

    private Npc npc() {
        return new Npc(TEST_TILE);
    }

    @Test
    void startsNpcsAtIndexOne() {
        NpcList npcs = new NpcList();
        Npc npc = npc();
        assertTrue(npcs.add(npc));
        assertEquals(1, npc.getIndex());
        assertSame(npc, npcs.get(1));
    }

    @Test
    void givesNpcsUniqueIndexes() {
        NpcList npcs = new NpcList();
        Npc first = npc();
        Npc second = npc();
        npcs.add(first);
        npcs.add(second);
        assertEquals(1, first.getIndex());
        assertEquals(2, second.getIndex());
        assertEquals(2, npcs.size());
    }

    @Test
    void removesNpcAndClearsIndex() {
        NpcList npcs = new NpcList();
        Npc npc = npc();
        npcs.add(npc);
        int index = npc.getIndex();
        assertTrue(npcs.remove(npc));
        assertEquals(Npc.NO_INDEX, npc.getIndex());
        assertNull(npcs.get(index));
        assertTrue(npcs.isEmpty());
    }

    @Test
    void doesNotRemoveDifferentNpcWithSameIndex() {
        NpcList npcs = new NpcList();
        NpcList otherNpcs = new NpcList();
        Npc npc = npc();
        Npc staleNpc = npc();
        npcs.add(npc);
        otherNpcs.add(staleNpc);
        assertEquals(npc.getIndex(), staleNpc.getIndex());
        assertFalse(npcs.remove(staleNpc));
        assertSame(npc, npcs.get(npc.getIndex()));
        assertEquals(1, npcs.size());
    }

    @Test
    void iteratesOverActiveNpcsOnly() {
        NpcList npcs = new NpcList();
        Npc first = npc();
        Npc second = npc();
        Npc third = npc();
        npcs.add(first);
        npcs.add(second);
        npcs.add(third);
        npcs.remove(second);
        List<Npc> found = new ArrayList<>();
        npcs.forEach(found::add);
        assertEquals(List.of(first, third), found);
    }

    @Test
    void reservesProtocolIndexes() {
        NpcList npcs = new NpcList();
        assertThrows(IndexOutOfBoundsException.class, () -> npcs.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> npcs.get(16383));
        assertEquals(16382, npcs.capacity());
    }

    @Test
    void reusesFreeIndexAfterWrapping() {
        NpcList npcs = new NpcList();
        Npc freedNpc = null;
        for (int index = 1; index <= NpcList.MAX_NPCS; index++) {
            Npc npc = npc();
            assertTrue(npcs.add(npc));
            if (index == 10000) {
                freedNpc = npc;
            }
        }
        assertTrue(npcs.isFull());
        assertFalse(npcs.add(npc()));
        int freedIndex = freedNpc.getIndex();
        assertTrue(npcs.remove(freedNpc));
        Npc replacement = npc();
        assertTrue(npcs.add(replacement));
        assertEquals(freedIndex, replacement.getIndex());
    }
}
