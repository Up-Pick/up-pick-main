package org.oneog.uppick.domain.auth.command.controller;

import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.auth.command.model.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.command.model.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.command.model.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.command.service.AuthCommandService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class AuthCommandController {

	private final AuthCommandService authCommandService;

	@PostMapping("/signup")
	public GlobalApiResponse<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {

		authCommandService.signup(signupRequest);
		return GlobalApiResponse.ok(null);

	}

	@PostMapping("/login")
	public GlobalApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

		LoginResponse loginResponse = authCommandService.login(loginRequest);
		return GlobalApiResponse.ok(loginResponse);
	}

}
