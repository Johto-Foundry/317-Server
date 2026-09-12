package server.net.login;

public enum LoginResponse {
    SUCCESS(2),
    INVALID_CREDENTIALS(3),
    ALREADY_ONLINE(5),
    OUT_OF_DATE(6),
    WORLD_FULL(7),
    SESSION_REJECTED(11);

    private final int code;

    LoginResponse(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
