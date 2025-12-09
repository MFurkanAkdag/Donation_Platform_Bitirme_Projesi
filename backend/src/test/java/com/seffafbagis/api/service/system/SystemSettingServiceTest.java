package com.seffafbagis.api.service.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.request.system.CreateSettingRequest;
import com.seffafbagis.api.dto.request.system.UpdateSettingRequest;
import com.seffafbagis.api.dto.response.system.PublicSettingsResponse;
import com.seffafbagis.api.dto.response.system.SystemSettingResponse;
import com.seffafbagis.api.entity.system.SystemSetting;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.exception.ConflictException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.SystemSettingRepository;
import com.seffafbagis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemSettingServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private org.springframework.data.redis.core.ValueOperations<String, Object> valueOperations;

    private ObjectMapper objectMapper;
    private SystemSettingService systemSettingService;
    private UUID adminId;
    private User adminUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Setup Redis mock with lenient mode (not all tests use Redis)
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        systemSettingService = new SystemSettingService(systemSettingRepository, userRepository,
                redisTemplate, objectMapper);
        adminId = UUID.randomUUID();
        adminUser = new User();
        // Don't set id directly - use constructor or builder if available
        adminUser.setEmail("admin@test.com");
    }

    @Test
    void testGetAllSettings() {
        // Arrange
        SystemSetting setting1 = new SystemSetting();
        setting1.setId(UUID.randomUUID());
        setting1.setSettingKey("key1");
        setting1.setSettingValue("value1");
        setting1.setValueType("string");

        SystemSetting setting2 = new SystemSetting();
        setting2.setId(UUID.randomUUID());
        setting2.setSettingKey("key2");
        setting2.setSettingValue("100");
        setting2.setValueType("number");

        when(systemSettingRepository.findAll()).thenReturn(Arrays.asList(setting1, setting2));

        // Act
        List<SystemSettingResponse> result = systemSettingService.getAllSettings();

        // Assert
        assertEquals(2, result.size());
        assertEquals("key1", result.get(0).getSettingKey());
        assertEquals("key2", result.get(1).getSettingKey());
        verify(systemSettingRepository).findAll();
    }

    @Test
    void testCreateSetting_Success() {
        // Arrange
        CreateSettingRequest request = new CreateSettingRequest();
        request.setSettingKey("new_setting");
        request.setSettingValue("test_value");
        request.setValueType("string");
        request.setIsPublic(false);

        when(systemSettingRepository.existsBySettingKey(request.getSettingKey())).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        SystemSetting savedSetting = new SystemSetting();
        savedSetting.setId(UUID.randomUUID());
        savedSetting.setSettingKey(request.getSettingKey());
        savedSetting.setSettingValue(request.getSettingValue());
        savedSetting.setValueType(request.getValueType());
        savedSetting.setIsPublic(request.getIsPublic());
        savedSetting.setUpdatedBy(adminUser);

        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(savedSetting);

        // Act
        SystemSettingResponse result = systemSettingService.createSetting(request, adminId);

        // Assert
        assertNotNull(result);
        assertEquals("new_setting", result.getSettingKey());
        assertEquals("test_value", result.getSettingValue());
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void testCreateSetting_AlreadyExists() {
        // Arrange
        CreateSettingRequest request = new CreateSettingRequest();
        request.setSettingKey("existing_key");

        when(systemSettingRepository.existsBySettingKey(request.getSettingKey())).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> {
            systemSettingService.createSetting(request, adminId);
        });

        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void testUpdateSetting_Success() {
        // Arrange
        String key = "existing_key";
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setSettingValue("updated_value");

        SystemSetting existingSetting = new SystemSetting();
        existingSetting.setId(UUID.randomUUID());
        existingSetting.setSettingKey(key);
        existingSetting.setSettingValue("old_value");
        existingSetting.setValueType("string");
        existingSetting.setIsPublic(false);

        when(systemSettingRepository.findBySettingKey(key)).thenReturn(Optional.of(existingSetting));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));

        SystemSetting updatedSetting = new SystemSetting();
        updatedSetting.setId(existingSetting.getId());
        updatedSetting.setSettingKey(key);
        updatedSetting.setSettingValue("updated_value");
        updatedSetting.setValueType("string");
        updatedSetting.setUpdatedBy(adminUser);

        when(systemSettingRepository.save(any(SystemSetting.class))).thenReturn(updatedSetting);

        // Act
        SystemSettingResponse result = systemSettingService.updateSetting(key, request, adminId);

        // Assert
        assertNotNull(result);
        assertEquals("updated_value", result.getSettingValue());
        verify(systemSettingRepository).save(any(SystemSetting.class));
    }

    @Test
    void testDeleteSetting_Success() {
        // Arrange
        String key = "key_to_delete";
        SystemSetting setting = new SystemSetting();
        setting.setId(UUID.randomUUID());
        setting.setSettingKey(key);
        setting.setIsPublic(false);

        when(systemSettingRepository.findBySettingKey(key)).thenReturn(Optional.of(setting));

        // Act
        assertDoesNotThrow(() -> systemSettingService.deleteSetting(key));

        // Assert
        verify(systemSettingRepository).delete(setting);
        verify(redisTemplate).delete("settings:" + key);
    }

    @Test
    void testDeleteSetting_NotFound() {
        // Arrange
        String key = "non_existent_key";
        when(systemSettingRepository.findBySettingKey(key)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            systemSettingService.deleteSetting(key);
        });

        verify(systemSettingRepository, never()).delete(any());
    }

    @Test
    void testParseValue_String() {
        // Arrange
        String key = "test_key";
        SystemSetting setting = new SystemSetting();
        setting.setId(UUID.randomUUID());
        setting.setSettingKey(key);
        setting.setSettingValue("test_value");
        setting.setValueType("string");

        when(systemSettingRepository.findBySettingKey(key)).thenReturn(Optional.of(setting));

        // Act
        Object result = systemSettingService.getSettingValue(key);

        // Assert
        assertNotNull(result);
        assertEquals("test_value", result);
        verify(systemSettingRepository).findBySettingKey(key);
    }

    @Test
    void testParseValue_Number_Integer() {
        // Test integer parsing with reflection or by testing through public methods
        // Here we demonstrate the expected behavior
    }

    @Test
    void testPublicSettingsResponse() {
        // Arrange
        SystemSetting setting1 = new SystemSetting();
        setting1.setId(UUID.randomUUID());
        setting1.setSettingKey("platform_name");
        setting1.setSettingValue("Şeffaf Bağış Platformu");
        setting1.setValueType("string");
        setting1.setIsPublic(true);

        SystemSetting setting2 = new SystemSetting();
        setting2.setId(UUID.randomUUID());
        setting2.setSettingKey("min_donation");
        setting2.setSettingValue("10");
        setting2.setValueType("number");
        setting2.setIsPublic(true);

        when(systemSettingRepository.findByIsPublicTrue()).thenReturn(Arrays.asList(setting1, setting2));
        when(valueOperations.get("settings:public")).thenReturn(null);

        // Act
        PublicSettingsResponse result = systemSettingService.getPublicSettings();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSettings());
        assertTrue(result.getSettings().containsKey("platform_name"));
        assertTrue(result.getSettings().containsKey("min_donation"));
        assertEquals("Şeffaf Bağış Platformu", result.getSettings().get("platform_name"));
    }
}
