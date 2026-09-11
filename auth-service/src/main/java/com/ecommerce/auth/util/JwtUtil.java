package com.ecommerce.auth.util;
import com.ecommerce.auth.entity.User;
import io.jsonwebtoken.Claims; import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.util.Date; import java.util.List; import java.util.Map;
@Component
public class JwtUtil {
 @Value("${jwt.secret}") private String secret;
 @Value("${jwt.expiration:3600000}") private Long expiration;
 private SecretKey key(){ return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
 public String generateToken(User user, List<String> roles){
   return Jwts.builder().claims(Map.of("email", user.getEmail(), "username", user.getUsername(), "roles", roles))
       .subject(user.getId().toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(key()).compact();
 }
 public boolean validateToken(String token){ try{ Jwts.parser().verifyWith(key()).build().parseSignedClaims(token); return true;}catch(Exception e){return false;} }
 public Claims extractClaims(String token){ return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload(); }
 public long getExpirationSeconds(){ return expiration/1000; }
}
