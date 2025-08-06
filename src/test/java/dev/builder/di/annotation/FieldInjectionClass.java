package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class FieldInjectionClass implements CommonI{

    @Inject
    private FooClass dependency;

    @Override
    public String commonString() {
        return "barClassString";
    }

    @Override
    public void superDo() {
    }

    public FooClass dependency() {
        return dependency;
    }
}
