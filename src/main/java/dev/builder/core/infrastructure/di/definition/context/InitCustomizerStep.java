package dev.builder.core.infrastructure.di.definition.context;

public interface InitCustomizerStep<T> extends BuildStep<T> {
    BuildStep<T> initCustomizer(InitCustomizer<T> initCustomizer);
}
