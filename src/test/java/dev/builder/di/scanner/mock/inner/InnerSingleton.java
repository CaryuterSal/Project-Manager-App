package dev.builder.di.scanner.mock.inner;

import dev.builder.core.infrastructure.di.annotation.Singleton;

@Singleton
public class InnerSingleton {

    @Singleton
    public static class InnerInnerSingleton{

    }
}
