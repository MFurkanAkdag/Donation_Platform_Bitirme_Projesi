package com.seffafbagis.api.config;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configures the {@link JavaMailSender} used for transactional emails.
 */
@Configuration
public class MailConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailConfig.class);

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String defaultFrom;

    public MailConfig(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${app.mail.default-from}") String defaultFrom) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.defaultFrom = defaultFrom;
    }

    /**
     * Creates the {@link JavaMailSender} bean with TLS and authentication enabled.
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        mailSender.setJavaMailProperties(buildMailProperties());
        logConfiguration();
        testConnection(mailSender);
        return mailSender;
    }

    private Properties buildMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.debug", "false");
        properties.put("mail.smtp.from", defaultFrom);
        return properties;
    }

    private void testConnection(JavaMailSenderImpl mailSender) {
        try {
            mailSender.testConnection();
            LOGGER.info("Mail server {}:{} is reachable", host, port);
        } catch (MessagingException ex) {
            LOGGER.warn("Mail server {}:{} is not reachable at startup: {}", host, port, ex.getMessage());
        }
    }

    private void logConfiguration() {
        LOGGER.info("Mail configuration -> host: {}, port: {}, username: {}, from: {}", host, port, username, defaultFrom);
    }
}
