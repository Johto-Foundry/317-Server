package server.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import server.world.World;
import server.world.player.Player;

import java.util.Objects;

final class SessionHandler extends ChannelInboundHandlerAdapter {

    private final World world;
    private final Session session;

    SessionHandler(World world, Session session) {
        this.world = Objects.requireNonNull(world, "world");
        this.session = Objects.requireNonNull(session, "session");
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Player player = session.getPlayer();
        if (player != null) {
            world.unregisterPlayer(player);
        }
        super.channelInactive(context);
    }
}
