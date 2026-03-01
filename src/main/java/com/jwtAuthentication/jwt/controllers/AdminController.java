package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.responseDto.AdminDashboardResponseDTO;
import com.jwtAuthentication.jwt.service.AdminService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponseDTO>> getDashboardCounts() {

        AdminDashboardResponseDTO data =
                adminService.getDashboardCounts();

        ApiResponse<AdminDashboardResponseDTO> response =
                new ApiResponse<>(
                        true,
                        "Dashboard counts fetched successfully",
                        data
                );

        return ResponseEntity.ok(response);
    }
}
