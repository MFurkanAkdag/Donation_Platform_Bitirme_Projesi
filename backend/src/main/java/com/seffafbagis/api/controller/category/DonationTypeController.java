package com.seffafbagis.api.controller.category;

import com.seffafbagis.api.dto.response.category.DonationTypeResponse;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.enums.DonationTypeCode;
import com.seffafbagis.api.service.category.DonationTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/donation-types")
public class DonationTypeController {

    private final DonationTypeService donationTypeService;

    public DonationTypeController(DonationTypeService donationTypeService) {
        this.donationTypeService = donationTypeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DonationTypeResponse>>> getAllDonationTypes() {
        List<DonationTypeResponse> types = donationTypeService.getAllDonationTypes();
        return ResponseEntity.ok(ApiResponse.success("All donation types retrieved successfully", types));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DonationTypeResponse>>> getActiveDonationTypes() {
        List<DonationTypeResponse> types = donationTypeService.getActiveDonationTypes();
        return ResponseEntity.ok(ApiResponse.success("Active donation types retrieved successfully", types));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<DonationTypeResponse>> getDonationTypeByCode(
            @PathVariable DonationTypeCode code) {
        DonationTypeResponse type = donationTypeService.getByTypeCode(code);
        return ResponseEntity.ok(ApiResponse.success("Donation type retrieved successfully", type));
    }
}
