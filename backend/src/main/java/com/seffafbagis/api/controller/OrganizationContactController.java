package com.seffafbagis.api.controller;

import com.seffafbagis.api.dto.request.organization.AddContactRequest;
import com.seffafbagis.api.dto.request.organization.UpdateContactRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.organization.OrganizationContactResponse;
import com.seffafbagis.api.service.organization.OrganizationContactService;
import com.seffafbagis.api.service.organization.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organization/contacts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDATION')")
public class OrganizationContactController {

    private final OrganizationContactService contactService;
    private final OrganizationService organizationService; // To get my org ID

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationContactResponse>>> getContacts() {
        // We can get organization ID from organizationService or let contactService
        // handle "my" contacts
        // contactService.getContacts(orgId) requires ID.
        // I will add a method or use getMyOrganization().getId()
        UUID orgId = organizationService.getMyOrganization().getId();
        List<OrganizationContactResponse> contacts = contactService.getContacts(orgId);
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> addContact(
            @Valid @RequestBody AddContactRequest request) {
        OrganizationContactResponse response = contactService.addContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> updateContact(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContactRequest request) {
        OrganizationContactResponse response = contactService.updateContact(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable UUID id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted successfully", null));
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<OrganizationContactResponse>> setPrimaryContact(@PathVariable UUID id) {
        OrganizationContactResponse response = contactService.setPrimaryContact(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
