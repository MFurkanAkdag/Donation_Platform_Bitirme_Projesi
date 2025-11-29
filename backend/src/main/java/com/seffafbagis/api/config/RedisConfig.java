package com.seffafbagis.api.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis yapılandırma sınıfı.
 * 
 * Redis şu amaçlarla kullanılır:
 * - Session/Token cache'leme
 * - Sık erişilen verilerin cache'lenmesi (kampanyalar, kategoriler)
 * - Rate limiting
 * - Refresh token saklama
 * 
 * GÜVENLİK NOTLARI:
 * - Production'da Redis password kullanın
 * - Redis'i sadece internal network'ten erişilebilir yapın
 * - TLS/SSL kullanmayı düşünün
 * 
 * @author Furkan
 * @version 1.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * Varsayılan cache süresi (dakika).
     */
    private static final long DEFAULT_CACHE_TTL_MINUTES = 60;

    /**
     * Kampanya cache süresi (dakika).
     * Kampanyalar sık değişmediği için daha uzun tutulabilir.
     */
    private static final long CAMPAIGN_CACHE_TTL_MINUTES = 30;

    /**
     * Kullanıcı cache süresi (dakika).
     * Güvenlik nedeniyle kısa tutulur.
     */
    private static final long USER_CACHE_TTL_MINUTES = 15;

    /**
     * Kategori cache süresi (dakika).
     * Kategoriler nadiren değişir.
     */
    private static final long CATEGORY_CACHE_TTL_MINUTES = 120;

    /**
     * Redis bağlantı fabrikası.
     * 
     * @return LettuceConnectionFactory instance
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        // Password varsa ayarla
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis template yapılandırması.
     * 
     * Key için String, Value için JSON serialization kullanılır.
     * Bu sayede Redis'teki veriler okunabilir formatta saklanır.
     * 
     * @param connectionFactory Redis bağlantı fabrikası
     * @return Yapılandırılmış RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key serializer - String olarak sakla
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value serializer - JSON olarak sakla
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }

    /**
     * Cache manager yapılandırması.
     * 
     * Farklı cache'ler için farklı TTL değerleri tanımlanır.
     * 
     * @param connectionFactory Redis bağlantı fabrikası
     * @return Yapılandırılmış CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Varsayılan cache yapılandırması
        RedisCacheConfiguration defaultConfig = createCacheConfiguration(DEFAULT_CACHE_TTL_MINUTES);
        
        // Cache bazında özel yapılandırmalar
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Kampanya cache'i
        cacheConfigurations.put("campaigns", createCacheConfiguration(CAMPAIGN_CACHE_TTL_MINUTES));
        cacheConfigurations.put("campaign", createCacheConfiguration(CAMPAIGN_CACHE_TTL_MINUTES));
        
        // Kullanıcı cache'i
        cacheConfigurations.put("users", createCacheConfiguration(USER_CACHE_TTL_MINUTES));
        cacheConfigurations.put("user", createCacheConfiguration(USER_CACHE_TTL_MINUTES));
        
        // Kategori cache'i
        cacheConfigurations.put("categories", createCacheConfiguration(CATEGORY_CACHE_TTL_MINUTES));
        cacheConfigurations.put("donationTypes", createCacheConfiguration(CATEGORY_CACHE_TTL_MINUTES));
        
        // Organizasyon cache'i
        cacheConfigurations.put("organizations", createCacheConfiguration(CAMPAIGN_CACHE_TTL_MINUTES));
        
        // Token blacklist (logout edilen token'lar)
        cacheConfigurations.put("tokenBlacklist", createCacheConfiguration(60 * 24)); // 24 saat
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Cache yapılandırması oluşturur.
     * 
     * @param ttlMinutes Cache süresi (dakika)
     * @return RedisCacheConfiguration
     */
    private RedisCacheConfiguration createCacheConfiguration(long ttlMinutes) {
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();
    }

    /**
     * JSON serializer oluşturur.
     * 
     * Java 8 tarih/saat tipleri ve polimorfik tipler için yapılandırılır.
     * 
     * @return GenericJackson2JsonRedisSerializer
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Java 8 tarih/saat desteği
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Polimorfik tip desteği (alt sınıfların doğru deserialize edilmesi için)
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
