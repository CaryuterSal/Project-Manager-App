package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class MixedInjectionClass {

    @Inject
    private BarClass fieldWiredBar;

    private FooClass notWiredFoo;

    private final BazClass constructorInjectedBaz;

    public MixedInjectionClass(BazClass baz){
        this.constructorInjectedBaz = baz;
    }

    public BarClass bar() {
        return fieldWiredBar;
    }

    public FooClass notWiredFoo() {
        return notWiredFoo;
    }

    public BazClass constructorInjectedBaz() {
        return constructorInjectedBaz;
    }
}
