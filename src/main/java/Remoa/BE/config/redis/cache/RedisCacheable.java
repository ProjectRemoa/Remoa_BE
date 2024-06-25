package Remoa.BE.config.redis.cache;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisCacheable {

    String cacheName();
    long expireTime() default -1;

}
