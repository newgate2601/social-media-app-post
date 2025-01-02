package com.example.social_media_app_post.security;

import com.example.social_media_app_post.common.Common;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;

// https://chatgpt.com/c/670be4e8-5540-8010-ace0-5d8035740fe0
// https://chatgpt.com/c/670be004-1844-8010-ad25-912561dce36f
@AllArgsConstructor
@Component
public class TokenHelper {
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days
    private final JwtDecoder jwtDecoder;

    public Long getUserIdFromToken(String token) {
        token = token.substring(7);
        Jwt decodedJwt = jwtDecoder.decode(token);
        // Extract the user_id claim
        return decodedJwt.getClaim("user_id");
    }

    public String getImageUrlFromToken(String token) {
        token = token.substring(7);
        Jwt decodedJwt = jwtDecoder.decode(token);
        return decodedJwt.getClaim(Common.IMAGE_URL);
    }

    public String getFullNameFromToken(String token) {
        token = token.substring(7);
        Jwt decodedJwt = jwtDecoder.decode(token);
        // Extract the user_id claim
        return decodedJwt.getClaim(Common.FULL_NAME);
    }
}
