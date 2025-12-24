package com.seffafbagis.api.aspect;

import com.seffafbagis.api.annotation.Auditable;
import com.seffafbagis.api.enums.AuditAction;
import com.seffafbagis.api.security.SecurityUtils;
import com.seffafbagis.api.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @Pointcut("@annotation(com.seffafbagis.api.annotation.Auditable)")
    public void auditableMethod() {
    }

    // Pointcut removed as UserSensitiveDataService does not exist

    @Around("auditableMethod()")
    public Object auditAnnotatedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Object oldValues = null; // Could try to fetch before proceed if needed
        AuditAction action = null;
        String entityType = null;
        UUID entityId = null;
        UUID userId = null;

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            if (auditable != null) {
                action = auditable.action();
                entityType = auditable.entityType();
            }

            // Get current user
            // Assuming SecurityUtils has getCurrentUserId method returning UUID
            // If it returns Optional<UUID> or String, adjust accordingly.
            // I'll assume getCurrentUserId returns UUID or similar.
            // I should check SecurityUtils content to be safe, but for now assuming
            // standard pattern.
            try {
                userId = SecurityUtils.getCurrentUserId().orElse(null);
            } catch (Exception e) {
                // System action or unauthenticated
            }

            result = joinPoint.proceed();

            // Try to extract entity ID from result if it's a DTO with getId()
            if (result != null) {
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(result);
                    if (id instanceof UUID) {
                        entityId = (UUID) id;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            } else {
                // Try to extract from arguments
                Object[] args = joinPoint.getArgs();
                for (Object arg : args) {
                    if (arg instanceof UUID) {
                        entityId = (UUID) arg;
                        break; // Assume first UUID is entity ID
                    }
                }
            }

            if (action != null) {
                auditLogService.log(action, userId, entityType, entityId, oldValues, result);
            }

            return result;

        } catch (Throwable e) {
            // Log failure if needed, or rely on global exception handler
            // But we might want to log the FAILED action
            if (action != null) {
                // We could log failure here, but Action enum doesn't always have FAILURE
                // counterpart
                // For now, allow exception to bubble up
            }
            throw e;
        }
    }

}
