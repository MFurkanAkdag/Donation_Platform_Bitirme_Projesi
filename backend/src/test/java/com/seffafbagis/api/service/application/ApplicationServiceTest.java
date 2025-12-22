package com.seffafbagis.api.service.application;

import com.seffafbagis.api.dto.mapper.ApplicationMapper;
import com.seffafbagis.api.dto.request.application.CreateApplicationRequest;
import com.seffafbagis.api.dto.request.application.UpdateApplicationRequest;
import com.seffafbagis.api.dto.response.application.ApplicationResponse;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.application.Application;
import com.seffafbagis.api.enums.ApplicationStatus;
import com.seffafbagis.api.repository.ApplicationRepository;
import com.seffafbagis.api.repository.CategoryRepository;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private ApplicationDocumentService documentService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private User currentUser;
    private Category category;
    private Application application;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("user@example.com");

        category = new Category();
        category.setId(UUID.randomUUID());

        application = new Application();
        application.setId(UUID.randomUUID());
        application.setApplicant(currentUser);
        application.setStatus(ApplicationStatus.PENDING);
        ReflectionTestUtils.setField(application, "createdAt", OffsetDateTime.now());
    }

    @Test
    void createApplication_Success() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of(currentUser.getEmail()));

            CreateApplicationRequest request = new CreateApplicationRequest();
            request.setCategoryId(category.getId());
            request.setTitle("Need help");
            request.setDescription("Help description");

            when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(applicationMapper.toEntity(any(), any(), any())).thenReturn(application);
            when(applicationRepository.save(any(Application.class))).thenReturn(application);
            when(applicationMapper.toResponse(any(Application.class))).thenReturn(new ApplicationResponse());

            ApplicationResponse response = applicationService.createApplication(request);

            assertNotNull(response);
            verify(applicationRepository).save(any(Application.class));
        }
    }

    @Test
    void updateApplication_Success_WhenPendingAndOwner() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of(currentUser.getEmail()));

            UpdateApplicationRequest request = new UpdateApplicationRequest();
            request.setTitle("Updated Title");

            when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
            when(applicationRepository.save(any(Application.class))).thenReturn(application);
            when(applicationMapper.toResponse(any(Application.class))).thenReturn(new ApplicationResponse());

            ApplicationResponse response = applicationService.updateApplication(application.getId(), request);

            assertNotNull(response);
            verify(applicationRepository).save(application);
            assertEquals("Updated Title", application.getTitle());
        }
    }

    @Test
    void updateApplication_Fail_WhenNotPending() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of(currentUser.getEmail()));

            application.setStatus(ApplicationStatus.APPROVED);
            UpdateApplicationRequest request = new UpdateApplicationRequest();

            when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            assertThrows(IllegalStateException.class,
                    () -> applicationService.updateApplication(application.getId(), request));
        }
    }

    @Test
    void cancelApplication_Success() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of(currentUser.getEmail()));

            when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

            applicationService.cancelApplication(application.getId());

            verify(applicationRepository).delete(application);
        }
    }

    @Test
    void viewApplication_Success_WhenOwner() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getCurrentUserEmail).thenReturn(Optional.of(currentUser.getEmail()));

            when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
            when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
            when(applicationMapper.toDetailResponse(application))
                    .thenReturn(new com.seffafbagis.api.dto.response.application.ApplicationDetailResponse());

            var response = applicationService.getMyApplication(application.getId());
            assertNotNull(response);
        }
    }
}
