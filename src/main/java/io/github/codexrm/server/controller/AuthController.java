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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService,
                          RefreshTokenService refreshTokenService,
                          PasswordEncoder encoder,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Register a new user account")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        userService.validateUser(signUpRequest.getUsername(), signUpRequest.getEmail());

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                signUpRequest.isEnabled(),
                encoder.encode(signUpRequest.getPassword())
        );

        userService.createUserAccount(user, true, null);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Operation(summary = "Authenticate user and generate JWT token")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        Date tokenDate = new Date(System.currentTimeMillis() + jwtUtils.getJwtExpirationMs());
        Date refreshTokenDate = Date.from(refreshToken.getExpiryDate());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                refreshToken.getToken(),
                tokenDate,
                refreshTokenDate,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getName(),
                userDetails.getLastName(),
                userDetails.isEnabled(),
                roles
        ));
    }

    @Operation(summary = "Generate a new JWT token using a refresh token")
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {

        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());

                    return ResponseEntity.ok(
                            new TokenRefreshResponse(
                                    token,
                                    requestRefreshToken,
                                    new Date(System.currentTimeMillis() + jwtUtils.getJwtExpirationMs())
                            )
                    );
                })
                .orElseThrow(() -> new TokenRefreshException(
                        requestRefreshToken,
                        "Refresh token is not in database!"
                ));
    }

    @Operation(summary = "Logout user and invalidate refresh tokens")
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(401)
                    .body(new MessageResponse("User not authenticated"));
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {

            refreshTokenService.deleteByUserId(userDetails.getId());
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new MessageResponse("Log out successful!"));
        }

        return ResponseEntity
                .status(401)
                .body(new MessageResponse("Invalid authentication principal"));
    }
}