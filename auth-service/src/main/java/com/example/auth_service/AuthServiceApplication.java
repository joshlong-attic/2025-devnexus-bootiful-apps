package com.example.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager (){
        var users = List.of(
            User.withUsername("jlong").password("pw").roles("USER").build(),
            User.withUsername("rwinch").password("pw").roles("USER","ADMIN").build()
        );
        return new InMemoryUserDetailsManager( users);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .with(authorizationServer(), as -> as.oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oneTimeTokenLogin(configurer -> configurer.tokenGenerationSuccessHandler(
                        (request, response, oneTimeToken) -> {
                            var msg = "go to http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue();
                            System.out.println(msg);
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                            response.getWriter().print("you've got console mail!");
                        }))
                .webAuthn(c -> c
                        .rpId("localhost")
                        .rpName("bootiful passkeys")
                        .allowedOrigins("http://localhost:8080")
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }
}
