package dev.builder.core.infrastructure.di.definition;

/**
 * Única forma de construcción válida para {@link BeanRegistrationConfiguration}.
 * </br>
 * El uso del patrón <a href="https://refactoring.guru/design-patterns/builder">Step Builder</a> provee una
 * API más coherente para el usuario, asegurando un estado consistente a la hora de la creación del {@link }
 *
 * @param <T> el tipo del bean a crear
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder pattern</a>
 */
public class BeanRegistrationBuilder<T> implements FirstStep<T>, OptionalConfigStep<T>, SingletonModeStep<T>{

    private final Class<T> clazz;
    private String beanName;
    private InitCustomizer<T> initCustomizer;
    private InstantiationMode instantiationMode;
    private BeanScope scope;

    private final BeanRegistrationEndBuilder<T> endBuilder = new BeanRegistrationEndBuilder<>(this);

    public BeanRegistrationBuilder(Class<T> clazz){
        this.clazz = clazz;
    }

    @Override
    public SingletonModeStep<T> singleton() {
        this.scope = BeanScope.SINGLETON;
        return this;
    }

    @Override
    public OptionalConfigStep<T> prototype() {
        this.scope = BeanScope.PROTOTYPE;
        return this;
    }

    @Override
    public OptionalConfigStep<T> asEagerSingleton() {
        this.scope = BeanScope.SINGLETON;
        this.instantiationMode = InstantiationMode.EAGER;
        return this;
    }

    @Override
    public OptionalConfigStep<T> asLazySingleton() {
        this.scope = BeanScope.SINGLETON;
        this.instantiationMode = InstantiationMode.LAZY;
        return this;
    }

    @Override
    public OptionalConfigStep<T> eager() {
        this.instantiationMode = InstantiationMode.EAGER;
        return this;
    }

    @Override
    public OptionalConfigStep<T> lazy() {
        this.instantiationMode = InstantiationMode.LAZY;
        return this;
    }

    @Override
    public BeanNameStep<T> initCustomizer(InitCustomizer<T> initCustomizer) {
        this.initCustomizer = initCustomizer;
        return endBuilder;
    }

    @Override
    public InitCustomizerStep<T> withName(String name) {
        this.beanName = name;
        return endBuilder;
    }

    @Override
    public BeanRegistrationConfiguration<T> build() {
        return endBuilder.build();
    }

    Class<T> clazz() {
        return clazz;
    }

    String beanName() {
        return beanName;
    }

    InitCustomizer<T> initCustomizer() {
        return initCustomizer;
    }

    InstantiationMode instantiationMode() {
        return instantiationMode;
    }

    BeanScope scope() {
        return scope;
    }

    private void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    private void setInitCustomizer(InitCustomizer<T> initCustomizer) {
        this.initCustomizer = initCustomizer;
    }

    public static class BeanRegistrationEndBuilder<T> implements BeanNameStep<T>, InitCustomizerStep<T>{

        private final BeanRegistrationBuilder<T> sourceBuilder;

        BeanRegistrationEndBuilder(BeanRegistrationBuilder<T> sourceBuilder){
            this.sourceBuilder = sourceBuilder;
        }

        @Override
        public BuildStep<T> withName(String name) {
            sourceBuilder.setBeanName(name);
            return this;
        }

        @Override
        public BuildStep<T> initCustomizer(InitCustomizer<T> initCustomizer) {
            sourceBuilder.setInitCustomizer(initCustomizer);
            return this;
        }

        @Override
        public BeanRegistrationConfiguration<T> build() {
            return new BeanRegistrationConfiguration<>(sourceBuilder.clazz(),sourceBuilder.beanName(), sourceBuilder.scope(), sourceBuilder.initCustomizer(), sourceBuilder.instantiationMode());
        }
    }
}
