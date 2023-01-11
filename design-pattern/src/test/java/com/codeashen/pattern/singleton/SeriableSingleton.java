package com.codeashen.pattern.singleton;

import java.io.Serializable;

public class SeriableSingleton implements Serializable {
    public final static SeriableSingleton INSTANCE = new SeriableSingleton();

    private SeriableSingleton() {
    }

    public static SeriableSingleton getInstance() {
        return INSTANCE;
    }
}
