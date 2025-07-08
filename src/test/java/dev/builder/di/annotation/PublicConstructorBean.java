package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Bean;

@Bean
public class PublicConstructorBean {
    private FooClass fooClass;

    public PublicConstructorBean(FooClass fooClass) {
        this.fooClass = fooClass;
    }

    public FooClass getFooClass() {
        return fooClass;
    }
}

