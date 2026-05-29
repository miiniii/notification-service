package com.mh.notification.infrastructure.redis;

final class RedisStreamExceptionUtils {

    private RedisStreamExceptionUtils() {
    }

    static boolean isNoGroup(Throwable throwable) {
        return containsMessage(throwable, "NOGROUP")
                || containsAllMessages(throwable, "No such key", "consumer group");
    }

    static boolean isBusyGroup(Throwable throwable) {
        return containsMessage(throwable, "BUSYGROUP");
    }

    static boolean isNoSuchStream(Throwable throwable) {
        return containsMessage(throwable, "no such key")
                || containsMessage(throwable, "No such key");
    }

    private static boolean containsMessage(Throwable throwable, String expected) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean containsAllMessages(Throwable throwable, String first, String second) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(first) && message.contains(second)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
