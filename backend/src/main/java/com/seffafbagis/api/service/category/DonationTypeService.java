package com.seffafbagis.api.service.category;

import com.seffafbagis.api.dto.mapper.CategoryMapper;
import com.seffafbagis.api.dto.response.category.DonationTypeResponse;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.enums.DonationTypeCode;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.DonationTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DonationTypeService {

    private final DonationTypeRepository donationTypeRepository;
    private final CategoryMapper categoryMapper;

    public DonationTypeService(DonationTypeRepository donationTypeRepository, CategoryMapper categoryMapper) {
        this.donationTypeRepository = donationTypeRepository;
        this.categoryMapper = categoryMapper;
    }

    public List<DonationTypeResponse> getAllDonationTypes() {
        return donationTypeRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<DonationTypeResponse> getActiveDonationTypes() {
        return donationTypeRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DonationTypeResponse getByTypeCode(DonationTypeCode code) {
        DonationType donationType = donationTypeRepository.findByTypeCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("DonationType", code.name()));
        return categoryMapper.toResponse(donationType);
    }
}
