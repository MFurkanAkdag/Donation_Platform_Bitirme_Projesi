package com.seffafbagis.api.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO for updating user profile.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Size(max = 500, message = "Avatar URL must be at most 500 characters")
    @URL(message = "Avatar URL must be a valid URL")
    private String avatarUrl;

    @Pattern(regexp = "^(tr|en)$", message = "Preferred language must be either 'tr' or 'en'")
    private String preferredLanguage;

    @Size(max = 50, message = "Timezone must be at most 50 characters")
    private String timezone;
}
