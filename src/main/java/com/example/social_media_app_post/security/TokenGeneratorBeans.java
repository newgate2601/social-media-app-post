package com.example.social_media_app_post.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.security.Security;
import java.util.UUID;

@Configuration
public class TokenGeneratorBeans {

    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        return new DelegatingOAuth2TokenGenerator(jwtGenerator);
//        jwtGenerator.setJwtCustomizer(customPayloadToken());
//
//        return new DelegatingOAuth2TokenGenerator(
//                jwtGenerator,
//                new OAuth2AccessTokenGenerator(),
//                new OAuth2RefreshTokenGenerator()
//        );
    }

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        return new NimbusJwtEncoder(jwkSource());
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        return NimbusJwtDecoder.withPublicKey(
                KeyConverter.getPublicKeyFromString(
                        Constants.PUBLIC_KEY
                )
        ).build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = getRSA();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey getRSA() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            return new RSAKey.Builder(
                    KeyConverter.getPublicKeyFromString(
                            Constants.PUBLIC_KEY
                    )
            )
                    .privateKey(
                            KeyConverter.getPrivateKeyFromString(
                                    Constants.PRIVATE_KEY
                            )
                    )
                    .keyID(UUID.randomUUID().toString()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
