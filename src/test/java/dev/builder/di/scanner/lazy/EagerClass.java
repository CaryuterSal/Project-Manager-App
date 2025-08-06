package dev.builder.di.scanner.lazy;

import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class EagerClass {

    private final LazyClass lazyClass;

    public EagerClass(LazyClass lazyClass) {
        this.lazyClass = lazyClass;
    }

    public LazyClass lazyClass() {
        return lazyClass;
    }

    String getMessage(){
        return "Secret Message";
    }
}
