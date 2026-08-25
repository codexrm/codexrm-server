package io.github.codexrm.server.api.controller;

import io.github.codexrm.server.api.dto.request.LoginRequest;
import io.github.codexrm.server.api.dto.request.SignupRequest;
import io.github.codexrm.server.api.dto.request.TokenRefreshRequest;
import io.github.codexrm.server.api.dto.response.ErrorResponse;
import io.github.codexrm.server.api.dto.response.JwtResponse;
import io.github.codexrm.server.api.dto.response.MessageResponse;
import io.github.codexrm.server.api.dto.response.TokenRefreshResponse;
import io.github.codexrm.server.infrastructure.exception.TokenRefreshException;
import io.github.codexrm.server.domain.model.RefreshToken;
import io.github.codexrm.server.infrastructure.security.jwt.JwtUtils;
import io.github.codexrm.server.infrastructure.security.services.RefreshTokenService;
import io.github.codexrm.server.infrastructure.security.services.UserDetailsImpl;
import io.github.codexrm.server.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService,
                          RefreshTokenService refreshTokenService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        userService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User registered successfully!"));
    }

    @Operation(summary = "Authenticate user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtils.generateJwtToken(userDetails);

            logger.info("event=auth.login.success username={}", userDetails.getUsername());

            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

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
                    roles));

        } catch (AuthenticationException e) {

            logger.warn("event=auth.login.failed username={}", loginRequest.getUsername());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            Instant.now(),
                            HttpStatus.UNAUTHORIZED.value(),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                            "Invalid username or password",
                            "/api/auth/signin"
                    ));
        }

    }

    @Operation(summary = "Refresh access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {

        String requestRefreshToken = request.getRefreshToken();

        RefreshToken verifiedToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> {
                    logger.warn("event=auth.refresh.failed reason=token_not_found");
                    return new TokenRefreshException(
                            requestRefreshToken,
                            "Refresh token is not in database!");
                });

        String username = verifiedToken.getUser().getUsername();
        RefreshToken rotatedToken = refreshTokenService.rotateRefreshToken(verifiedToken);

        logger.info("event=auth.refresh.success username={}", username);
        String jwt = jwtUtils.generateTokenFromUsername(username);

        return ResponseEntity.ok(
                new TokenRefreshResponse(
                        jwt,
                        rotatedToken.getToken(),
                        new Date(System.currentTimeMillis() + jwtUtils.getJwtExpirationMs())
                )
        );
    }

    @Operation(summary = "Logout user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logoutUser() {

            UserDetailsImpl userDetails =
                    (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            logger.info("event=auth.logout username={}", userDetails.getUsername());

            refreshTokenService.deleteByUserId(userDetails.getId());
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new MessageResponse("Log out successful!"));
    }
}