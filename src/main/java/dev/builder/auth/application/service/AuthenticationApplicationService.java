package dev.builder.auth.application.service;

import dev.builder.auth.application.command.LoginCommand;
import dev.builder.auth.infrastructure.SessionToken;
import dev.builder.auth.domain.port.in.AuthenticationService;
import dev.builder.auth.domain.port.out.SessionContext;
import dev.builder.auth.domain.port.out.SessionStorage;
import dev.builder.core.infrastructure.di.annotation.Bean;
import dev.builder.core.infrastructure.properties.MessageLocalizer;
import dev.builder.usermanagement.domain.model.Password;
import dev.builder.usermanagement.domain.model.User;
import dev.builder.usermanagement.domain.port.out.AnyUserRepository;
import dev.builder.usermanagement.domain.port.out.PasswordMatcher;

import java.util.Optional;

@Bean
public class AuthenticationApplicationService implements AuthenticationService {

    private final AnyUserRepository userRepository;
    private final SessionStorage sessionStorage;
    private final SessionContext sessionContext;
    private final MessageLocalizer messageLocalizer;
    private final PasswordMatcher passwordMatcher;

    public AuthenticationApplicationService(AnyUserRepository userRepository, MessageLocalizer messageLocalizer, SessionContext sessionContext, SessionStorage sessionStorage, PasswordMatcher passwordMatcher) {
        this.userRepository = userRepository;
        this.sessionStorage = sessionStorage;
        this.passwordMatcher = passwordMatcher;
        this.sessionContext = sessionContext;
        this.messageLocalizer = messageLocalizer;
    }

    @Override
    public void login(LoginCommand loginCommand) {
        User<?> currentUser = userRepository.findById(new User.Id<>(loginCommand.email())).orElseThrow(
                () -> new UnauthorizedException(messageLocalizer.getMessage("auth.bad.credentials"))
        );
        if(!currentUser.login(new Password(loginCommand.password()), passwordMatcher)){
            throw new UnauthorizedException(messageLocalizer.getMessage("auth.bad.credentials"));
        }
        sessionContext.setAuthentication(currentUser);
        sessionStorage.saveSession();
    }

    @Override
    public void logout() {
        sessionContext.clear();
        sessionStorage.clearSession();
    }

    @Override
    public boolean restoreSession() {
        Optional<SessionToken> storedSession = sessionStorage.restoreSession();
        if(storedSession.isEmpty()) return false;
        Optional<? extends User<?>> currentUser = userRepository.findById(new User.Id<>(storedSession.get().email()));
        if(currentUser.isEmpty()) return false;
        sessionContext.setAuthentication(currentUser.get());
        return true;
    }
}
