package dev.builder.di.scanner.lazy;

import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.di.annotation.Inject;
import dev.builder.core.infrastructure.di.annotation.PostConstruct;

@Bean
public class LazyClass {

    private EagerClass eagerClass;
    private String receivedMessage;

    @Inject
    public void setEagerClass(EagerClass eagerClass) {
        this.eagerClass = eagerClass;
    }

    public EagerClass eagerClass() {
        return eagerClass;
    }

    public String receivedMessage() {
        return receivedMessage;
    }

    @PostConstruct
    private void init(){
        this.receivedMessage = eagerClass.getMessage();
    }
}
