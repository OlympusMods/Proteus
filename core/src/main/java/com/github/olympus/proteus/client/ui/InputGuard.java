package com.github.olympus.proteus.client.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InputGuard {
    private static final Logger LOGGER = LoggerFactory.getLogger("proteus");
    private static final long QUIET_NANOS = 60_000_000_000L;
    private static final int MAX_CAUSES = 64;
    private static final Map<String, Long> LAST = new HashMap<>();

    private InputGuard() {
    }

    public static boolean run(String what, BooleanSupplier action) {
        try {
            return action.getAsBoolean();
        } catch (Throwable throwable) {
            report(what, throwable);
            return false;
        }
    }

    private static void report(String what, Throwable throwable) {
        StackTraceElement[] trace = throwable.getStackTrace();
        String site = trace.length > 0 ? trace[0].toString() : "";
        String cause = what + "|" + throwable.getClass().getName() + "|" + site;
        long now = System.nanoTime();
        Long last = LAST.get(cause);
        if (last != null && now - last < QUIET_NANOS) {
            return;
        }
        if (LAST.size() >= MAX_CAUSES) {
            LAST.clear();
        }
        LAST.put(cause, now);
        LOGGER.warn("Proteus: {} failed, ignoring it", what, throwable);
    }
}
