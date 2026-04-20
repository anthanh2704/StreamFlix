package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.RegisterRequest;
import com.streamflix.dto.UserResponse;
import com.streamflix.entity.User;
import com.streamflix.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        User u = userService.register(req);
        return ApiResponse.ok("Registration successful", UserResponse.fromEntity(u));
    }

    /** Relies on HTTP Basic: if credentials are accepted, returns the current user. */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        User u = userService.findByUsername(principal.getUsername());
        return ApiResponse.ok(UserResponse.fromEntity(u));
    }
}
