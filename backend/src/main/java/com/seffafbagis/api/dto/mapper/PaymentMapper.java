package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.response.payment.SavedCardResponse;
import com.seffafbagis.api.dto.response.payment.TransactionResponse;
import com.seffafbagis.api.entity.donation.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "processedAt", source = "processedAt")
    TransactionResponse toResponse(Transaction transaction);

    default java.time.LocalDateTime map(java.time.OffsetDateTime value) {
        return value != null ? value.toLocalDateTime() : null;
    }

    /**
     * Maps card information from Iyzico response to SavedCardResponse DTO.
     * This is a manual mapping method since Iyzico card data comes in different
     * formats.
     */
    default SavedCardResponse toSavedCardResponse(String cardToken, String cardAlias, String lastFour, String brand,
            String family) {
        return SavedCardResponse.builder()
                .cardToken(cardToken)
                .cardAlias(cardAlias)
                .cardLastFour(lastFour)
                .cardBrand(brand)
                .cardFamily(family)
                .build();
    }
}
