package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.donation.CreateDonationRequest;
import com.seffafbagis.api.dto.response.donation.*;
import com.seffafbagis.api.entity.campaign.Campaign;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.entity.donation.Donation;
import com.seffafbagis.api.entity.donation.DonationReceipt;
import com.seffafbagis.api.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    public Donation toEntity(CreateDonationRequest request, Campaign campaign, User donor, DonationType donationType) {
        Donation donation = new Donation();
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setDonationType(donationType);
        donation.setAmount(request.getAmount());
        donation.setCurrency("TRY");
        donation.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        donation.setDonorMessage(request.getDonorMessage());

        // If not anonymous, use request display name or fallback to user's name
        if (Boolean.TRUE.equals(donation.getIsAnonymous())) {
            donation.setDonorDisplayName("Anonim Bağışçı");
        } else {
            if (request.getDonorDisplayName() != null && !request.getDonorDisplayName().isBlank()) {
                donation.setDonorDisplayName(request.getDonorDisplayName());
            } else if (donor != null) {
                donation.setDonorDisplayName(donor.getFullName());
            } else {
                donation.setDonorDisplayName("Anonim Bağışçı"); // Fallback if no user and no display name
            }
        }

        return donation;
    }

    public DonationResponse toResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        mapCommonFields(donation, response);
        return response;
    }

    public DonationDetailResponse toDetailResponse(Donation donation) {
        DonationDetailResponse response = new DonationDetailResponse();
        mapCommonFields(donation, response);

        response.setRefundStatus(donation.getRefundStatus());
        response.setRefundReason(donation.getRefundReason());

        if (donation.getReceipt() != null) {
            response.setReceipt(toReceiptResponse(donation.getReceipt()));
        }

        return response;
    }

    public DonationListResponse toListResponse(Page<Donation> donationPage) {
        Page<DonationResponse> responses = donationPage.map(this::toResponse);
        // Stats can be calculated separately or passed in
        return new DonationListResponse(responses, null, donationPage.getTotalElements());
    }

    public DonorListResponse toDonorListResponse(Donation donation) {
        DonorListResponse response = new DonorListResponse();

        if (Boolean.TRUE.equals(donation.getIsAnonymous())) {
            response.setDonorDisplayName("Anonim Bağışçı");
        } else {
            response.setDonorDisplayName(donation.getDonorDisplayName());
        }

        response.setAmount(donation.getAmount());
        response.setMessage(donation.getDonorMessage());
        response.setCreatedAt(donation.getCreatedAt().toLocalDateTime());

        return response;
    }

    public DonationReceiptResponse toReceiptResponse(DonationReceipt receipt) {
        if (receipt == null)
            return null;

        DonationReceiptResponse response = new DonationReceiptResponse();
        response.setId(receipt.getId());
        response.setDonationId(receipt.getDonation().getId());
        response.setReceiptNumber(receipt.getReceiptNumber());
        response.setGeneratedAt(receipt.getIssuedAt());

        // Map redundant fields from receipt if they exist, or fetch from
        // donation/relations
        // Assuming Receipt entity might store snapshot data, but for now using
        // relations
        Donation donation = receipt.getDonation();

        if (Boolean.TRUE.equals(donation.getIsAnonymous())) {
            response.setDonorName("Anonim Bağışçı");
        } else {
            response.setDonorName(donation.getDonorDisplayName());
        }

        response.setCampaignTitle(donation.getCampaign().getTitle());
        response.setOrganizationName(donation.getCampaign().getOrganization().getLegalName());
        response.setAmount(donation.getAmount());
        response.setDonationDate(donation.getCreatedAt());

        return response;
    }

    private void mapCommonFields(Donation donation, DonationResponse response) {
        response.setId(donation.getId());
        response.setCampaignId(donation.getCampaign().getId());
        response.setCampaignTitle(donation.getCampaign().getTitle());
        response.setCampaignSlug(donation.getCampaign().getSlug());

        if (donation.getDonationType() != null) {
            response.setDonationTypeCode(donation.getDonationType().getTypeCode().name());
            response.setDonationTypeName(donation.getDonationType().getName());
        }

        response.setAmount(donation.getAmount());
        response.setCurrency(donation.getCurrency());
        response.setStatus(donation.getStatus().name());
        response.setIsAnonymous(donation.getIsAnonymous());
        response.setDonorMessage(donation.getDonorMessage());

        if (Boolean.TRUE.equals(donation.getIsAnonymous())) {
            response.setDonorDisplayName("Anonim Bağışçı");
        } else {
            response.setDonorDisplayName(donation.getDonorDisplayName());
        }

        response.setCreatedAt(donation.getCreatedAt());
    }
}
