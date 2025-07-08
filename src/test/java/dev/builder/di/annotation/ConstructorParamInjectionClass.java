package dev.builder.di.annotation;

import dev.builder.core.infrastructure.di.annotation.Inject;

public class ConstructorParamInjectionClass {
    private final BarClass injectable1;
    private final FooClass injectable2;
    private final BazClass unused;

    ConstructorParamInjectionClass(@Inject BarClass injectable1, @Inject FooClass injectable2){
        this.injectable1 = injectable1;
        this.injectable2 = injectable2;
        this.unused = null;
    }

    ConstructorParamInjectionClass(@Inject BarClass injectable1, BazClass unused, FooClass injectable2){
        this.injectable1 = injectable1;
        this.injectable2 = injectable2;
        this.unused = unused;
    }

    ConstructorParamInjectionClass(@Inject FooClass injectable2, BazClass unused){
        this.injectable2 = injectable2;
        this.unused = unused;
        this.injectable1 = null;
    }

    public BarClass getInjectable1() {
        return injectable1;
    }

    public FooClass getInjectable2() {
        return injectable2;
    }

    public BazClass getUnused() {
        return unused;
    }
}
