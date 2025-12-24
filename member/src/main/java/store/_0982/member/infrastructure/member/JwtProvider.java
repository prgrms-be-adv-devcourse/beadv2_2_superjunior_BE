package store._0982.member.infrastructure.member;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import store._0982.member.domain.member.Member;

public class JwtProvider {

    private final Key key;
    private final long accessTokenValidityPeriod;
    private final long refreshTokenValidityPeriod;

    public JwtProvider(String secretKey, long accessTokenValidityPeriod, long refreshTokenValidityPeriod) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityPeriod = accessTokenValidityPeriod;
        this.refreshTokenValidityPeriod = refreshTokenValidityPeriod;
    }

    public String generateAccessToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityPeriod);

        return Jwts.builder()
                .setSubject(member.getMemberId().toString())
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .setIssuer("member-service")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityPeriod);

        return Jwts.builder()
                .setSubject(member.getMemberId().toString())
                .claim("email", member.getEmail())
                .claim("role", member.getRole().name())
                .setIssuer("member-service")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID getMemberIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
