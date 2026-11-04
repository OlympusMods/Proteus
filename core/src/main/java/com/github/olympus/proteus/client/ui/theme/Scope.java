package com.github.olympus.proteus.client.ui.theme;

import java.util.Map;

public interface Scope {
    Object lookup(String name);

    default boolean exists(String resource) {
        return false;
    }

    static Scope of(Map<String, ?> values) {
        return values::get;
    }

    static Scope chain(Scope... scopes) {
        return new Scope() {
            @Override
            public Object lookup(String name) {
                for (Scope scope : scopes) {
                    Object value = scope.lookup(name);
                    if (value != null) {
                        return value;
                    }
                }
                return null;
            }

            @Override
            public boolean exists(String resource) {
                for (Scope scope : scopes) {
                    if (scope.exists(resource)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
