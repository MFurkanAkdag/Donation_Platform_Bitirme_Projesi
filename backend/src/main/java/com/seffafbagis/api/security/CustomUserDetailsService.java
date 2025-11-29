package com.seffafbagis.api.security;

import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Spring Security UserDetailsService implementasyonu.
 * 
 * Bu servis, kullanıcı bilgilerini veritabanından yükler.
 * Spring Security authentication sırasında bu servisi kullanır.
 * 
 * @author Furkan
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * Constructor.
     * 
     * @param userRepository User repository
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Kullanıcıyı e-posta adresine göre yükler.
     * 
     * Spring Security bu metodu authentication sırasında çağırır.
     * 
     * @param email Kullanıcı e-posta adresi (username olarak kullanılıyor)
     * @return UserDetails nesnesi
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Boş email kontrolü
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Boş email ile kullanıcı arama denemesi");
            throw new UsernameNotFoundException("E-posta adresi boş olamaz");
        }

        // E-postayı küçük harfe çevir (case-insensitive arama için)
        String normalizedEmail = email.toLowerCase().trim();

        // Kullanıcıyı bul
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        // Kullanıcı bulunamadıysa hata fırlat
        if (userOptional.isEmpty()) {
            logger.warn("Kullanıcı bulunamadı: {}", normalizedEmail);
            throw new UsernameNotFoundException("Kullanıcı bulunamadı: " + normalizedEmail);
        }

        User user = userOptional.get();

        logger.debug("Kullanıcı yüklendi: {}", normalizedEmail);

        // CustomUserDetails nesnesine dönüştür
        return new CustomUserDetails(user);
    }
}
