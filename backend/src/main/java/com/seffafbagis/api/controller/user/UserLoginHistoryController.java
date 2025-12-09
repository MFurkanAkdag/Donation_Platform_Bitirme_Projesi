package com.seffafbagis.api.controller.user;

import com.seffafbagis.api.dto.response.audit.LoginHistoryResponse;
import com.seffafbagis.api.dto.response.common.PageResponse;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.audit.LoginHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "User Login History", description = "User's own login history")
public class UserLoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping("/login-history")
    @Operation(summary = "Get my login history")
    public PageResponse<LoginHistoryResponse> getMyLoginHistory(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return PageResponse.of(loginHistoryService.getUserLoginHistory(userId, pageable));
    }
}
