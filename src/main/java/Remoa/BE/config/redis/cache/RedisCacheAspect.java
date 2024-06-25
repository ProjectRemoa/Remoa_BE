package Remoa.BE.config.redis.cache;

import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(RedisCacheable)")
    public Object cacheableProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisCacheable redisCacheable = getCacheableAnnotation(method);

        final String cacheKey = generateCacheKey(redisCacheable, method, joinPoint);

        if (redisTemplate.hasKey(cacheKey) && !shouldBypassCache()) {
            log.info("Getting data from cache: {}", cacheKey);
            return redisTemplate.opsForValue().get(cacheKey);
        }

        log.info("Cache miss for key: {}, executing method", cacheKey);
        final Object methodReturnValue = joinPoint.proceed();
        final long cacheTTL = redisCacheable.expireTime();

        if (cacheTTL < 0) {
            redisTemplate.opsForValue().set(cacheKey, methodReturnValue);
            log.info("Storing data in cache: {}", cacheKey);
        } else {
            redisTemplate.opsForValue().set(cacheKey, methodReturnValue, cacheTTL, TimeUnit.MINUTES);
            log.info("Storing data in cache: {}, TTL: {} minutes", cacheKey, cacheTTL);
        }

        return methodReturnValue;
    }

    private RedisCacheable getCacheableAnnotation(Method method) {
        return AnnotationUtils.getAnnotation(method, RedisCacheable.class);
    }

    private String generateCacheKey(RedisCacheable redisCacheable, Method method, ProceedingJoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder(redisCacheable.cacheName());

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        boolean hasRedisCachedKeyParam = false;

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof RedisCachedKeyParam) {
                    hasRedisCachedKeyParam = true;
                    RedisCachedKeyParam keyParam = (RedisCachedKeyParam) annotation;
                    keyBuilder.append(":").append(keyParam.key()).append("=");

                    if (keyParam.fields().length > 0) {
                        for (String field : keyParam.fields()) {
                            keyBuilder.append(getFieldValue(args[i], field)).append(":");
                        }
                        // 마지막 콜론 제거
                        if (keyBuilder.charAt(keyBuilder.length() - 1) == ':') {
                            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                        }
                    } else {
                        // 필드가 지정되지 않은 경우 매개변수 값을 직접 사용
                        keyBuilder.append(args[i]);
                    }
                }
            }
        }
        if (!hasRedisCachedKeyParam) {
            keyBuilder.append(":").append(method.getName());
        }
        return keyBuilder.toString();
    }

    private boolean isComplexObjectType(Class<?> clazz) {
        return !clazz.isPrimitive() && !clazz.equals(String.class) && !Number.class.isAssignableFrom(clazz);
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new BaseException(CustomMessage.ANNOTATION_ERROR);
        }
    }

    private boolean shouldBypassCache() {
        return false;  // Default implementation
    }

    @Around("@annotation(EvictRedisCache)")
    public Object evictRedisCache(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        EvictRedisCache evictRedisCache = method.getAnnotation(EvictRedisCache.class);

        String cacheKey = generateEvictionCacheKey(evictRedisCache, method, joinPoint);
        redisTemplate.delete(cacheKey);
        log.info("Evicted cache for key: {}", cacheKey);

        return joinPoint.proceed();
    }

    private String generateEvictionCacheKey(EvictRedisCache evictRedisCache, Method method, ProceedingJoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder(evictRedisCache.cacheName());

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        boolean hasRedisCachedKeyParam = false;

        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof RedisCachedKeyParam) {
                    hasRedisCachedKeyParam = true;
                    RedisCachedKeyParam keyParam = (RedisCachedKeyParam) annotation;
                    keyBuilder.append(":").append(keyParam.key()).append("=");

                    for (String field : keyParam.fields()) {
                        keyBuilder.append(getFieldValue(args[i], field)).append(":");
                    }

                    if (keyBuilder.length() > 0 && keyBuilder.charAt(keyBuilder.length() - 1) == ':') {
                        keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                    }
                }
            }
        }

        if (!hasRedisCachedKeyParam) {
            keyBuilder.append(":").append(method.getName());
        }

        return keyBuilder.toString();
    }
}
