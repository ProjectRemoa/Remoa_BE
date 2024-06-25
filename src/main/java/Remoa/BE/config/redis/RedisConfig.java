package Remoa.BE.config.redis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolvers;
import io.lettuce.core.resource.MappingSocketAddressResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
public class RedisConfig implements CachingConfigurer {

    private final RedisInfo redisInfo;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        List<String> nodes = redisInfo.getNodes();
        String connectIp = redisInfo.getConnectIp();
        int maxRedirects = redisInfo.getMaxRedirects();
        System.out.println("nodes = " + nodes);
        System.out.println("connectIp = " + connectIp);
        System.out.println("maxRedirects = " + maxRedirects);

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(nodes);

        redisClusterConfiguration.setMaxRedirects(redisInfo.getMaxRedirects());

        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAllAdaptiveRefreshTriggers()
                .enablePeriodicRefresh(Duration.ofHours(1L))
                .build();
        ClientOptions clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();

        MappingSocketAddressResolver resolver = MappingSocketAddressResolver.create(DnsResolvers.UNRESOLVED,
                hostAndPort -> {
                    HostAndPort andPort = HostAndPort.of(redisInfo.getConnectIp(), hostAndPort.getPort());
                    return andPort;
                }
        );

        ClientResources clientResources = ClientResources.builder()
                .socketAddressResolver(resolver)
                .build();

        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .clientOptions(clientOptions)
                .clientResources(clientResources) //<--- 추가
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(redisClusterConfiguration, clientConfiguration);

        return connectionFactory;
    }

    //JSON 직렬화/역직렬화 관련
    private ObjectMapper redisObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfSubType(Object.class)
                .build();

        return new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());


        // Key는 String, Value는 ResRegisteredTag로 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // Hash 데이터 구조를 위한 설정 (필요에 따라 추가)
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> cacheName.concat(":"))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(redisObjectMapper())
                ));
    }

    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        return RedisCacheManager.builder(lettuceConnectionFactory) // 자동 주입된 LettuceConnectionFactory 사용
                .cacheDefaults(this.cacheConfiguration())
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(exception.getMessage(), exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn(exception.getMessage(), exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn(exception.getMessage(), exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn(exception.getMessage(), exception);
            }
        };
    }
}