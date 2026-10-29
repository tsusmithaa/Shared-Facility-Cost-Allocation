
package com.sharedfacility.gateway;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/dev")
public class TokenController {

    private static final String SECRET =
            "SharedFacilityJwtSecretKey2026!!";

    @GetMapping("/token")
    public String generateToken(
            @RequestParam(defaultValue = "accountant") String role) {

        if (!role.equals("accountant")
                && !role.equals("facilityManager")) {
            throw new IllegalArgumentException("Invalid role");
        }

        SecretKey key = new SecretKeySpec(
                SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        Instant now = Instant.now();

        return Jwts.builder()
                .subject("demo-user")
                .claim("roles", List.of(role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}
