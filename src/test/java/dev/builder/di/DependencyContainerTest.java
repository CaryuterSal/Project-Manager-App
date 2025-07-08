package dev.builder.di;

import dev.builder.core.infrastructure.di.runtime.DefaultDependencyContainer;
import dev.builder.core.infrastructure.di.runtime.DependencyContainer;
import dev.builder.core.infrastructure.di.definition.BeanRegistrationConfiguration;
import dev.builder.core.infrastructure.di.definition.BeanScope;
import dev.builder.core.infrastructure.di.definition.InitCustomizer;
import dev.builder.core.infrastructure.di.definition.InstantiationMode;
import dev.builder.core.infrastructure.di.exception.BeanNotFoundException;
import dev.builder.core.infrastructure.di.exception.UncertainBeanRetrievalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.logging.Logger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DependencyContainerTest {

    private static final DependencyContainer container = DefaultDependencyContainer.getInstance();

    private static  class FooClass{
        private String message;
        FooClass(){
        }

        String getMessage(){
            return message;
        }
        void setMessage(String message){
            this.message = message;
        }
    }

    public interface SuperContract{
    }

    public interface Contract extends SuperContract{
        String saySomething();
    }

    private static final class FirstContract extends FooClass implements Contract{

        public FirstContract() {
        }

        @Override
        public String saySomething() {
            return "Hello from FIRST contract";
        }
    }

    private static final class SecondContract implements Contract{
        public SecondContract() {
        }

        @Override
        public String saySomething() {
            return "Hello from SECOND contract";
        }
    }

    private static  class BarClass{
        final FooClass foo;
        final Contract contract;

        BarClass(FooClass fooClass, Contract contract){
            this.foo = fooClass;
            this.contract = contract;
        }

        public void initFoo(String message){
            foo.setMessage(message);
        }

        public String askFoo(){
            return foo.getMessage();
        }

        public String userContract(){
            return contract.saySomething();
        }
    }

    @BeforeEach
    public void setUp() {
        container.clear();
    }

    @Test
    void testBeanRegistrationConfiguration(){
        String testBeanName = "RareBean";
        InitCustomizer<FooClass> initCustomizer = (c) -> c.setMessage("HelloWorld");
        BeanRegistrationConfiguration<FooClass> config = BeanRegistrationConfiguration.builder(FooClass.class)
                .singleton()
                .eager()
                .initCustomizer(initCustomizer)
                .withName(testBeanName)
                .build();
        assertThat(config).isNotNull();
        assertAll(
                () -> assertThat(config.clazz()).isEqualTo(FooClass.class),
                () -> assertThat(config.beanName().get()).isEqualTo(testBeanName),
                () -> assertThat(config.beanScope()).isEqualTo(BeanScope.SINGLETON),
                () -> assertThat(config.instatiationMode().get()).isEqualTo(InstantiationMode.EAGER),
                () -> assertThat(config.initCustomizer().get()).isEqualTo(initCustomizer)
        );
    }


    @Test
    void testRegisterBeanWithClassSucceeds(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .singleton()
                .lazy()
                .build());
        assertThat(container.isRegistered(FooClass.class)).isTrue();
    }

    @Test
    void testRegisterEagerBeanRegisters(){
        final CreatedHelper created = new CreatedHelper();
        assertThat(created.created()).isFalse();
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asEagerSingleton()
                        .initCustomizer((x) -> created.setCreated())
                .build());
        assertThat(container.isRegistered(FooClass.class)).isTrue();
        assertThat(created.created()).isTrue();
    }

    private static final class CreatedHelper{
        private boolean created = false;

        CreatedHelper(){
            created = false;
        }

        public void setCreated(){
            created = true;
        }

        public boolean created() {
            return created;
        }
    }

    @Test
    void testSingletonBeanReturnsSameInstance(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton().build());
        container.isSingleton(FooClass.class);
        FooClass firstClass = container.getInstance(FooClass.class);
        FooClass secondClass = container.getInstance(FooClass.class);
        assertThat(firstClass).isEqualTo(secondClass);
    }

    @Test
    void testUnregisteredBeanIsNotFound(){
        assertThrowsExactly(BeanNotFoundException.class, () -> container.isSingleton(FooClass.class));
    }

    @Test
    void testPrototypeBeanIsNotSingleton(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .prototype().build());
        assertFalse(container.isSingleton(FooClass.class));
    }

    @Test
    void testRegisterSameBeanReturnsFalse(){
        boolean registered = container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton().build());
        assertThat(container.isRegistered(FooClass.class)).isTrue();
        assertThat(registered).isTrue();
        boolean sameIsRegistered = container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton().build());

        assertThat(container.isRegistered(FooClass.class)).isTrue();
        assertThat(sameIsRegistered).isFalse();
    }

    @Test
    void testRegisterBeanAndRetrieveAsInterface(){
        container.register(BeanRegistrationConfiguration.builder(FirstContract.class)
                .asLazySingleton().build());
        assertThat(container.isRegistered(Contract.class)).isTrue();
    }

    @Test
    void testRegisterBeanWithSameInterfaceIsAmbiguous(){
        container.register(BeanRegistrationConfiguration.builder(FirstContract.class)
                .asLazySingleton().build());
        container.register(BeanRegistrationConfiguration.builder(SecondContract.class)
                .asLazySingleton().build());
        assertThat(container.isRegistered(Contract.class)).isTrue();
        assertDoesNotThrow(() -> container.getInstance(FirstContract.class));
        assertThrowsExactly(UncertainBeanRetrievalException.class,() -> container.getInstance(Contract.class));
    }

    @Test
    void testBeanIsNotRegistered(){
        assertThat(container.isRegistered(Contract.class)).isFalse();
        assertThat(container.isRegistered(FooClass.class)).isFalse();
        container.register(BeanRegistrationConfiguration.builder(BarClass.class)
                .asLazySingleton().build());
        container.clear();
        assertThat(container.isRegistered(BarClass.class)).isFalse();
    }
    
    @Test
    void testRegisterMultipleBeansWithSameClassFails(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton().build());
        container.register(BeanRegistrationConfiguration.builder(BarClass.class)
                .asLazySingleton().build());

    }

    @Test
    void testRegisterAndRetrieveWithName(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .prototype().build());
        assertThat(container.isRegistered("fooClass")).isTrue();
        FooClass fooClass = container.getInstance(FooClass.class);
        assertThat(fooClass).isNotNull();
    }

    @Test
    void testRegisterPrototypeReturnsOtherInstance(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .prototype().build());
        FooClass firstInstance =  container.getInstance(FooClass.class);
        FooClass secondInstance = container.getInstance(FooClass.class);
        assertThat(firstInstance).isNotEqualTo(secondInstance);
    }

    @Test
    void testRetrieveBeanWithCustomName(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton()
                .withName("customName")
                .build());
        assertTrue(container.isSingleton("customName"));
        assertTrue(container.isRegistered("customName"));
        assertFalse(container.isRegistered("fooClass"));

        FooClass foo = container.getInstance(FooClass.class,"customName");
    }


    @Test
    void testResolveConstructorParamsSucceeds(){
        container.register(BeanRegistrationConfiguration.builder(BarClass.class)
                .asLazySingleton().build());
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
                .asLazySingleton().build());
        container.register(BeanRegistrationConfiguration.builder(SecondContract.class)
                .prototype().build());
        BarClass bar = container.getInstance(BarClass.class);
        assertNotNull(bar);
        assertAll(
                () -> assertThat(bar.userContract()).isEqualTo("Hello from SECOND contract"),
                () -> {
                    String mockMessage = "custom init message";
                    bar.initFoo(mockMessage);
                    assertThat(bar.askFoo()).isEqualTo(mockMessage);
                }
        );
    }

    @Test
    void testRetrieveRegisteredTypesReturnsOnly(){
        container.register(BeanRegistrationConfiguration.builder(FooClass.class)
            .asLazySingleton().build());
        container.register(BeanRegistrationConfiguration.builder(FirstContract.class)
                .asLazySingleton().build());
        assertThat(container.getRegisteredTypes()).isEqualTo(Set.of(FooClass.class, FirstContract.class, Contract.class, SuperContract.class));
        assertThat(container.getRegisteredBeanNames()).isEqualTo(Set.of("fooClass", "firstContract"));
    }

    @Test
    void testResolveRegisterForInterfaceFails(){
        assertThrowsExactly(IllegalArgumentException.class, () -> container.register(BeanRegistrationConfiguration.builder(Contract.class)
                .asLazySingleton().build()));
    }
}
