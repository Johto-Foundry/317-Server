package server.net.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.net.Session;
import server.world.World;
import server.world.map.Tile;
import server.world.player.Player;
import server.world.player.PlayerAdmission;

import java.util.Locale;
import java.util.Objects;

public final class LoginHandler extends SimpleChannelInboundHandler<LoginRequest> {

    private static final Tile DEFAULT_SPAWN = new Tile(3222, 3218, 0);

    private final World world;
    private final Session session;

    public LoginHandler(World world, Session session) {
        this.world = Objects.requireNonNull(world, "world");
        this.session = Objects.requireNonNull(session, "session");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, LoginRequest request) {
        if (request.reconnecting()) {
            reject(context, LoginResponse.SESSION_REJECTED);
            return;
        }

        String username = normalizeUsername(request.username());
        if (!validUsername(username) || request.password().isEmpty()) {
            reject(context, LoginResponse.INVALID_CREDENTIALS);
            return;
        }

        Player player = new Player(username, DEFAULT_SPAWN);
        world.admitPlayer(player).whenComplete((result, error) ->
                context.executor().execute(() -> finishLogin(context, session, player, request, result, error))
        );
    }

    private void finishLogin(
            ChannelHandlerContext context,
            Session session,
            Player player,
            LoginRequest request,
            PlayerAdmission result,
            Throwable error
    ) {
        if (error != null) {
            context.close();
            return;
        }

        if (!context.channel().isActive()) {
            if (result == PlayerAdmission.ACCEPTED) {
                world.unregisterPlayer(player);
            }
            return;
        }

        switch (result) {
            case ALREADY_ONLINE -> reject(context, LoginResponse.ALREADY_ONLINE);
            case WORLD_FULL -> reject(context, LoginResponse.WORLD_FULL);
            case ACCEPTED -> accept(context, session, player, request);
        }
    }

    private void accept(ChannelHandlerContext context, Session session, Player player, LoginRequest request) {
        session.initializeIsaac(request.isaacSeed());
        session.attachPlayer(player);

        ByteBuf response = context.alloc().buffer(3);
        response.writeByte(LoginResponse.SUCCESS.code());
        response.writeByte(0);
        response.writeByte(0);

        context.writeAndFlush(response).addListener(future -> {
            if (!future.isSuccess()) {
                world.unregisterPlayer(player);
                context.close();
                return;
            }

            if (context.pipeline().context(LoginDecoder.class) != null) {
                context.pipeline().remove(LoginDecoder.class);
            }
            if (context.pipeline().context(this) != null) {
                context.pipeline().remove(this);
            }
        });
    }

    private void reject(ChannelHandlerContext context, LoginResponse response) {
        context.writeAndFlush(context.alloc().buffer(1).writeByte(response.code()))
                .addListener(ChannelFutureListener.CLOSE);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private boolean validUsername(String username) {
        if (username.isEmpty() || username.length() > 12) {
            return false;
        }

        for (int index = 0; index < username.length(); index++) {
            char character = username.charAt(index);
            if (!Character.isLetterOrDigit(character) && character != ' ') {
                return false;
            }
        }
        return true;
    }
}
