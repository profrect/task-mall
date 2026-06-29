package com.mall.common.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUtils {

    /**
     * 默认有效时间（10分钟）
     */
    private static final long DEFAULT_EXPIRE_TIME = 1000L * 60 * 10;

    private static final String DEFAULT_KEY = "fK$2mQ#9vL@5xP&7bR!3nT^K9Ijht#(1cA)0dS+5gH=8kM%2pU#9rX@4vB!7eF^6tN*3wY(5jC)1hZ=";

    public static String generateTokenByDefaultKey(String subject, Map<String, Object> claims){
        return generateToken(subject, claims, DEFAULT_KEY);
    }

    public static String generateToken(String subject, Map<String, Object> claims, String secret) {
        return generateToken(subject, claims, secret, DEFAULT_EXPIRE_TIME);
    }

    public static String generateToken(String subject, Map<String, Object> claims, String secret, long expireMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(secret))
                .compact();
    }

    public static Claims parseToken(String token, String secret) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey(secret))
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }

    public static Claims parseTokenByDefaultKey(String token){
        return parseToken(token, DEFAULT_KEY);
    }

    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("--->> token已过期：", e);
            return false;
        } catch (Exception e) {
            log.warn("--->> 验证token出现异常：", e);
            return false;
        }
    }

    public static boolean validateTokenByDefaultKey(String token){
        return validateToken(token, DEFAULT_KEY);
    }

    private static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
