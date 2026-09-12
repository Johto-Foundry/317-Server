package server.net.login;

public record LoginRequest(
        String username,
        String password,
        int uid,
        boolean reconnecting,
        boolean lowMemory,
        int[] cacheCrcs,
        int[] isaacSeed
) {
    public LoginRequest {
        if (username == null) {
            throw new NullPointerException("username");
        }
        if (password == null) {
            throw new NullPointerException("password");
        }
        cacheCrcs = cacheCrcs.clone();
        isaacSeed = isaacSeed.clone();
    }

    @Override
    public int[] cacheCrcs() {
        return cacheCrcs.clone();
    }

    @Override
    public int[] isaacSeed() {
        return isaacSeed.clone();
    }
}
