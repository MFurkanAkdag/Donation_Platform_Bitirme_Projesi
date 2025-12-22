package com.seffafbagis.api.service.category;

import com.seffafbagis.api.dto.mapper.CategoryMapper;
import com.seffafbagis.api.dto.response.category.DonationTypeResponse;
import com.seffafbagis.api.entity.category.DonationType;
import com.seffafbagis.api.enums.DonationTypeCode;
import com.seffafbagis.api.repository.DonationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationTypeServiceTest {

    @Mock
    private DonationTypeRepository donationTypeRepository;

    @Spy
    private CategoryMapper categoryMapper = new CategoryMapper();

    @InjectMocks
    private DonationTypeService donationTypeService;

    private DonationType donationType;

    @BeforeEach
    void setUp() {
        donationType = new DonationType();
        donationType.setId(UUID.randomUUID());
        donationType.setTypeCode(DonationTypeCode.ZEKAT);
        donationType.setName("Zakat");
        donationType.setActive(true);
    }

    @Test
    void getActiveDonationTypes_ShouldReturnActiveTypes() {
        when(donationTypeRepository.findByIsActiveTrueOrderByNameAsc())
                .thenReturn(Collections.singletonList(donationType));

        List<DonationTypeResponse> result = donationTypeService.getActiveDonationTypes();

        assertFalse(result.isEmpty());
        assertEquals(DonationTypeCode.ZEKAT, result.get(0).getTypeCode());
        verify(donationTypeRepository).findByIsActiveTrueOrderByNameAsc();
    }

    @Test
    void getByTypeCode_ShouldReturnCorrectType() {
        when(donationTypeRepository.findByTypeCode(DonationTypeCode.ZEKAT))
                .thenReturn(Optional.of(donationType));

        DonationTypeResponse result = donationTypeService.getByTypeCode(DonationTypeCode.ZEKAT);

        assertNotNull(result);
        assertEquals(DonationTypeCode.ZEKAT, result.getTypeCode());
    }
}
