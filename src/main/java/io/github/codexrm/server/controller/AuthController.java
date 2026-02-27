package io.github.codexrm.server.controller;

import io.github.codexrm.server.exception.TokenRefreshException;
import io.github.codexrm.server.model.RefreshToken;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.payload.request.*;
import io.github.codexrm.server.payload.response.*;
import io.github.codexrm.server.security.jwt.JwtUtils;
import io.github.codexrm.server.security.services.RefreshTokenService;
import io.github.codexrm.server.security.services.UserDetailsImpl;
import io.github.codexrm.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, RefreshTokenService refreshTokenService, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        String exist = userService.validateUser(signUpRequest.getUsername(), signUpRequest.getEmail());

        if (exist.equals("Error: Username is already taken!"))
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));

        if (exist.equals("Error: Email is already in use!"))
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));

        // Create new user's account
        User user = new User(signUpRequest.getUsername(), signUpRequest.getName(),
                signUpRequest.getLastName(), signUpRequest.getEmail(), signUpRequest.isEnabled(),
                encoder.encode(signUpRequest.getPassword()));

        userService.createUserAccount(user, true, null);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        Date tokenDate = new Date((new Date()).getTime() + jwtUtils.getJwtExpirationMs());
        Date refreshTokenDate = Date.from(refreshToken.getExpiryDate());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), tokenDate, refreshTokenDate, userDetails.getId(),
                userDetails.getUsername(), userDetails.getEmail(), userDetails.getName(),
                userDetails.getLastName(), userDetails.isEnabled(), roles));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {

        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken, new Date((new Date()).getTime() + jwtUtils.getJwtExpirationMs())));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer userId = userDetails.getId();

        refreshTokenService.deleteByUserId(userId);
        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}

