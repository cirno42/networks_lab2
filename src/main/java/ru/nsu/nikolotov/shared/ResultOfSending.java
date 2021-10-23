package ru.nsu.nikolotov.shared;

public enum ResultOfSending {

    ERROR(0), SUCCESSFUL(1), NOT_FINISHED(2);
    private final int code;

    ResultOfSending(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResultOfSending getByCode(int code) {
        return switch (code) {
            case 1 -> SUCCESSFUL;
            case 2 -> NOT_FINISHED;
            default -> ERROR;
        };
    }

    public String toString() {
        return switch (this.code) {
            case 1 -> "SUCCESSFUL";
            case 2 -> "NOT_FINISHED";
            default -> "ERROR";
        };
    }
}
