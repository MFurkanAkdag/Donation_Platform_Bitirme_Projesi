package com.seffafbagis.api.service.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seffafbagis.api.dto.request.system.CreateSettingRequest;
import com.seffafbagis.api.dto.request.system.UpdateSettingRequest;
import com.seffafbagis.api.dto.response.system.PublicSettingsResponse;
import com.seffafbagis.api.dto.response.system.SystemSettingResponse;
import com.seffafbagis.api.entity.system.SystemSetting;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ConflictException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.SystemSettingRepository;
import com.seffafbagis.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SystemSettingService {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingService.class);
    private static final String CACHE_KEY_PREFIX = "settings:";
    private static final String PUBLIC_SETTINGS_CACHE_KEY = "settings:public";
    private static final long CACHE_TTL_HOURS = 1;

    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public SystemSettingService(SystemSettingRepository systemSettingRepository,
            UserRepository userRepository,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.systemSettingRepository = systemSettingRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<SystemSettingResponse> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(SystemSettingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SystemSettingResponse getSettingByKey(String key) {
        String cacheKey = CACHE_KEY_PREFIX + key;

        // Try cache
        try {
            SystemSettingResponse cached = (SystemSettingResponse) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            logger.error("Redis cache error: {}", e.getMessage());
        }

        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        SystemSettingResponse response = SystemSettingResponse.fromEntity(setting);

        // Store in cache
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("Redis set error: {}", e.getMessage());
        }

        return response;
    }

    public PublicSettingsResponse getPublicSettings() {
        // Try cache
        try {
            PublicSettingsResponse cached = (PublicSettingsResponse) redisTemplate.opsForValue()
                    .get(PUBLIC_SETTINGS_CACHE_KEY);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            logger.error("Redis cache error: {}", e.getMessage());
        }

        List<SystemSetting> publicSettings = systemSettingRepository.findByIsPublicTrue();
        Map<String, Object> settingsMap = new HashMap<>();

        for (SystemSetting setting : publicSettings) {
            settingsMap.put(setting.getSettingKey(), parseValue(setting.getSettingValue(), setting.getValueType()));
        }

        PublicSettingsResponse response = new PublicSettingsResponse(settingsMap);

        // Store in cache
        try {
            redisTemplate.opsForValue().set(PUBLIC_SETTINGS_CACHE_KEY, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("Redis set error: {}", e.getMessage());
        }

        return response;
    }

    @Transactional
    public SystemSettingResponse createSetting(CreateSettingRequest request, UUID adminId) {
        if (systemSettingRepository.existsBySettingKey(request.getSettingKey())) {
            throw new ConflictException("SystemSetting", "settingKey", request.getSettingKey());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(request.getSettingKey());
        setting.setSettingValue(request.getSettingValue());
        setting.setValueType(request.getValueType());
        setting.setDescription(request.getDescription());
        setting.setIsPublic(request.getIsPublic());
        setting.setUpdatedBy(admin);

        SystemSetting saved = systemSettingRepository.save(setting);

        if (Boolean.TRUE.equals(request.getIsPublic())) {
            invalidatePublicCache();
        }

        return SystemSettingResponse.fromEntity(saved);
    }

    @Transactional
    public SystemSettingResponse updateSetting(String key, UpdateSettingRequest request, UUID adminId) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        boolean previouslyPublic = setting.getIsPublic();

        if (request.getSettingValue() != null) {
            setting.setSettingValue(request.getSettingValue());
        }
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            setting.setIsPublic(request.getIsPublic());
        }

        setting.setUpdatedBy(admin);
        SystemSetting updated = systemSettingRepository.save(setting);

        invalidateCache(key);

        // Invalidate public cache if:
        // 1. Setting was public
        // 2. Setting is now public
        // 3. Public visibility changed
        if (previouslyPublic || Boolean.TRUE.equals(updated.getIsPublic())) {
            invalidatePublicCache();
        }

        return SystemSettingResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteSetting(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        boolean wasPublic = setting.getIsPublic();
        systemSettingRepository.delete(setting);

        invalidateCache(key);
        if (wasPublic) {
            invalidatePublicCache();
        }
    }

    public Object getSettingValue(String key) {
        SystemSettingResponse setting = getSettingByKey(key);
        return parseValue(setting.getSettingValue(), setting.getValueType());
    }

    public Object getSettingValueOrDefault(String key, Object defaultValue) {
        try {
            return getSettingValue(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void invalidateCache(String key) {
        try {
            redisTemplate.delete(CACHE_KEY_PREFIX + key);
            logger.info("Invalidated cache for key: {}", key);
        } catch (Exception e) {
            logger.error("Redis delete error: {}", e.getMessage());
        }
    }

    private void invalidatePublicCache() {
        try {
            redisTemplate.delete(PUBLIC_SETTINGS_CACHE_KEY);
            logger.info("Invalidated public settings cache");
        } catch (Exception e) {
            logger.error("Redis delete error: {}", e.getMessage());
        }
    }

    private Object parseValue(String value, String valueType) {
        if (value == null)
            return null;
        try {
            switch (valueType.toLowerCase()) {
                case "number":
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    } else {
                        return Long.parseLong(value);
                    }
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "json":
                    return objectMapper.readValue(value, Object.class);
                case "string":
                default:
                    return value;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse setting value '{}' as type '{}'. Returning as string.", value, valueType);
            return value;
        }
    }
}
