package dev.builder.core.infrastructure.di.annotation;

import dev.builder.core.infrastructure.di.definition.InstantiationMode;
import dev.builder.core.infrastructure.di.exception.UnsupportedFieldInjectionException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class BeanAnnotationAccessors {

    /**
     * Determina si una clase tiene la anotación {@link Bean} o {@link Singleton}
     * @param clazz la clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedBean(Class<?> clazz) {
        Class<?> beanclass = Objects.requireNonNull(clazz, "clazz cannot be null");
        return beanclass.isAnnotationPresent(Bean.class) || beanclass.isAnnotationPresent(Singleton.class);
    }

    /**
     * Determina si la anotación {@link Bean} está presente
     * @param clazz la clase a revisar
     * @return verdadero si está presente
     */
    public static boolean isBeanAnnotationPresent(Class<?> clazz){
        return Objects.requireNonNull(clazz).isAnnotationPresent(Bean.class);
    }
    /**
     * Determina si una clase tiene la anotación {@link Singleton} o si tiene la {@link Bean} como singleton
     * @param clazz la clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedSingleton(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            Bean annotation = clazz.getAnnotation(Bean.class);
            return annotation.singleton();
        } else {
            return clazz.isAnnotationPresent(Singleton.class);
        }
    }

    /**
     * Determina si un bean debe siempre se debe instance do como Lazy,
     * según el modo de {@link Singleton} o {@link Bean}, y si está presente la anotación {@link Lazy}
     * @param clazz la clase target
     * @return verdadero si es Lazy
     */
    public static boolean isAnnotatedLazy(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            return clazz.getAnnotation(Bean.class).mode().equals(InstantiationMode.LAZY);
        } else if(isAnnotatedSingleton(clazz)) {
            return clazz.getAnnotation(Singleton.class).mode().equals(InstantiationMode.LAZY);
        } else {
            return clazz.isAnnotationPresent(Lazy.class);
        }
    }

    /**
     * Determina si un bean define explícitamente si quiere usar el modo Prototype,
     * según el modo de {@link Singleton} o {@link Bean}, y si está presente la anotación {@link Eager}
     * @param clazz la clase target
     * @return verdadero si es prototype explícito
     */
    public static boolean isAnnotatedEager(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            return clazz.getAnnotation(Bean.class).mode().equals(InstantiationMode.EAGER);
        } else if(isAnnotatedSingleton(clazz)) {
            return clazz.getAnnotation(Singleton.class).mode().equals(InstantiationMode.EAGER);
        } else {
            return clazz.isAnnotationPresent(Eager.class);
        }
    }

    /**
     * Determina si un constructor tiene la anotación {@link Inject}
     * @param constructor el constructor target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedInjectConstructor(Constructor<?> constructor){
        return Objects.requireNonNull(constructor, "constructor cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un campo de clase tiene la anotación {@link Inject}
     * @param field el campo de clase target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedInjectField(Field field) {
        return Objects.requireNonNull(field, "field cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un parámetro tiene la anotación {@link Inject}
     * @param param el parámetro target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedInjectParam(Parameter param) {
        return Objects.requireNonNull(param, "param cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Determina si un método tiene la anotación {@link Inject}
     * @param method el método target
     * @return verdadero si tiene la anotación, falso si no
     */
    public static boolean isAnnotatedInjectMethod(Method method) {
        return Objects.requireNonNull(method, "method cannot be null").isAnnotationPresent(Inject.class);
    }

    /**
     * Extrae el nombre de bean de una clase anotada
     * @param clazz la clase target
     * @return el nombre de bean, o un optional vacío si no se declara en las anotaciones
     */
    public static Optional<String> extractAnnotatedBeanName(Class<?> clazz) {
        if(isBeanAnnotationPresent(clazz)) {
            return Optional.ofNullable(clazz.getAnnotation(Bean.class).name());
        } else if(isAnnotatedSingleton(clazz)) {
            return Optional.ofNullable(clazz.getAnnotation(Singleton.class).name());
        }
        return Optional.empty();
    }

    /**
     * Filtra una lista de constructores para aquellos que tengan la anotación {@link Inject}
     * @param constructors la lista de constructores
     * @return lista filtrada de constructores
     */
    public static <T> List<Constructor<T>> getInjectAnnotatedConstructors(@NotNull List<Constructor<T>> constructors) {
        return constructors.stream()
                .filter(BeanAnnotationAccessors::isAnnotatedInjectConstructor)
                .toList();
    }


    /**
     * Obtiene el número de parámetros del constructor
     * @param constructor el constructor target. No puede ser {@code null}
     * @return la cantidad de parámetros, {@code 0} si no tiene ninguno
     */
    public static int getAnnotatedParamCount(@NotNull Constructor<?> constructor){
        return getInjectableParams(constructor).size();
    }

    /**
     * Obtiene la lista de parámetros del constructor que estén anotados con {@link Inject}
     * </br>
     * La está ordenada conforme están declarados los parámetros en el constructor
     * @param constructor el constructor target
     * @return la lista con los parámetros, o lista vacía si ningún parámetro tiene la anotación
     */
    public static List<Parameter> getInjectableParams(@NotNull Constructor<?> constructor){
        Parameter[] params = constructor.getParameters();
        return Arrays.stream(params)
                .filter(BeanAnnotationAccessors::isAnnotatedInjectParam)
                .toList();
    }

    /**
     * Busca los campos que están anotados con {@link Inject} en una clase
     * @param clazz la clase target
     * @return los campos anotados, o un set vacío si no tiene ninguno
     */
    public static @NotNull Set<Field> getAnnotatedFields(@NotNull Class<?> clazz){
        Set<Field> annotatedFields = new HashSet<>();
        for(Field field : clazz.getDeclaredFields()){
            if(isAnnotatedInjectField(field)){
                int modifiers = field.getModifiers();
                if(Modifier.isFinal(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject final fields");
                if(Modifier.isStatic(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject static fields");
                if(Modifier.isTransient(modifiers)) throw new UnsupportedFieldInjectionException("Cannot inject transient fields");
                annotatedFields.add(field);
            }
        }
        return annotatedFields;
    }
}
