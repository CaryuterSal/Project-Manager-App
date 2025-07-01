package dev.builder.core.infrastructure.di;

import org.jetbrains.annotations.NotNull;

public class BeanContainerException extends RuntimeException {
  public BeanContainerException(String beanName) {
    super("Bean with name " + beanName + " not found");
  }
  public BeanContainerException(@NotNull Class<?> clazz) {
    super("Bean for class " + clazz.getSimpleName() + " not found");
  }
}
