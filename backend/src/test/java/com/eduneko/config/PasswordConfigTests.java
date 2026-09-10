package com.eduneko.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest 
public class PasswordConfigTests {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void debeGenerarHashYValidarPassword() {

        String password = "MiClave123!";

        String hash = passwordEncoder.encode(password);

        assertThat(hash).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, hash)).isTrue();
        assertThat(passwordEncoder.matches("ClaveIncorrecta", hash)).isFalse();
    }
}
