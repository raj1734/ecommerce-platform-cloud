package com.ecommerce.auth.service;
import com.ecommerce.auth.dto.*; import com.ecommerce.auth.entity.*; import com.ecommerce.auth.exception.*; import com.ecommerce.auth.repository.*; import com.ecommerce.auth.util.JwtUtil;
import io.jsonwebtoken.Claims; import lombok.RequiredArgsConstructor; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID;
@Service @RequiredArgsConstructor
public class AuthService {
 private final UserRepository userRepository; private final RoleRepository roleRepository; private final UserRoleRepository userRoleRepository; private final PasswordEncoder passwordEncoder; private final JwtUtil jwtUtil;
 @Transactional public RegisterResponse register(RegisterRequest r){
   if(userRepository.existsByUsername(r.getUsername())) throw new UserAlreadyExistsException("Username already exists");
   if(userRepository.existsByEmail(r.getEmail())) throw new UserAlreadyExistsException("Email already exists");
   User u=new User(); u.setUsername(r.getUsername()); u.setEmail(r.getEmail()); u.setPasswordHash(passwordEncoder.encode(r.getPassword())); u.setStatus("ACTIVE"); u.setPasswordChangedAt(LocalDateTime.now());
   u=userRepository.save(u);
   Role role=roleRepository.findByName("USER").orElseGet(()->{Role x=new Role();x.setName("USER");x.setDescription("Default customer role");return roleRepository.save(x);});
   userRoleRepository.save(new UserRole(new UserRole.UserRoleId(u.getId(),role.getId())));
   return RegisterResponse.builder().userId(u.getId().toString()).username(u.getUsername()).message("User registered successfully").build();
 }
 @Transactional public AuthResponse login(LoginRequest r){
   User u=userRepository.findByUsername(r.getUsername()).orElseThrow(()->new InvalidCredentialsException("Invalid credentials"));
   if(!"ACTIVE".equals(u.getStatus()) || !passwordEncoder.matches(r.getPassword(),u.getPasswordHash())) throw new InvalidCredentialsException("Invalid credentials");
   u.setFailedLoginAttempts(0); u.setLastLoginAt(LocalDateTime.now()); userRepository.save(u); return tokenResponse(u,rolesFor(u.getId()));
 }
 public boolean validateToken(String token){return jwtUtil.validateToken(token);}
 public ValidateResponse validate(String token){ if(!jwtUtil.validateToken(token)) return ValidateResponse.builder().valid(false).build(); Claims c=jwtUtil.extractClaims(token); Object roles=c.get("roles"); List<String> rs=roles instanceof List<?> l?l.stream().map(Object::toString).toList():List.of("USER"); return ValidateResponse.builder().valid(true).userId(c.getSubject()).roles(rs).build(); }
 public MessageResponse logout(String token){return new MessageResponse("Logout successful");}
 private List<String> rolesFor(UUID userId){ return userRoleRepository.findAllByIdUserId(userId).stream().map(x -> roleRepository.findById(x.getId().getRoleId()).map(Role::getName).orElse("USER")).toList(); }
 private AuthResponse tokenResponse(User u,List<String> roles){return new AuthResponse(jwtUtil.generateToken(u,roles),"Bearer",jwtUtil.getExpirationSeconds());}
}
