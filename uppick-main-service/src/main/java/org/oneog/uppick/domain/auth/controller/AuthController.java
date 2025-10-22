package org.oneog.uppick.domain.auth.controller;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.auth.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public GlobalApiResponse<Void> signup(
		@Valid @RequestBody
		SignupRequest signupRequest) {

		authService.signup(signupRequest);

		return GlobalApiResponse.ok(null);

	}

	@PostMapping("/login")
	public GlobalApiResponse<LoginResponse> login(
		@Valid @RequestBody
		LoginRequest loginRequest) {
		LoginResponse loginResponse = authService.login(loginRequest);

		return GlobalApiResponse.ok(loginResponse);
	}

}
