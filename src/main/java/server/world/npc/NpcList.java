package server.world.npc;

import server.world.entity.EntityList;

public final class NpcList extends EntityList<Npc> {

    public static final int MAX_NPCS = 16382;

    public NpcList() {
        super(MAX_NPCS);
    }
}
