package dev.builder.core.infrastructure.di.definition.context;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Representa configuraciones extra opcionales que pueden aplicarse al momento que se crea un bean
 * @param <T> tipo del bean
 */
@FunctionalInterface
public interface InitCustomizer<T> extends UnaryOperator<T> {
}
