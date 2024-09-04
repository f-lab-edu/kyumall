package com.kyumall.kyumallcommon.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisCacheConfig {
  @Value("${spring.redis.host}")
  private String host;
  @Value("${spring.redis.port}")
  private Integer port;

  /**
   * 레디스 커넥션 설정
   * Connectors
   *  - Spring Data Redis 에서 지원하는 자바 Redis Connectors 에는 Jedis와 Lettuce 가 있다.
   *  - 비동기 I/O 서버인 Netty를 사용하는 Lettuce 를 선택하였다.
   * 단일 레디스를 사용하므로, ConnectionFactory Modes 설정을 Standalone 으로 둔다.
   * @return
   */
  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
  }

  /**
   * Spring Cache의 RedisCacheManager 빈
   * default 캐시 설정을 정의하고, Cache 별로 직접 정의할 수 있다.
   * 범용적인 객체를 Json으로 직렬화/역직렬화 하기 위해 GenericJackson2JsonRedisSerializer 를 사용하였고, ObjectMapper 를 직접 정의하여 사용한다.
   * @param redisConnectionFactory
   * @return
   */
  @Bean
  public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues() // Null 값 캐시 안함
        .entryTtl(Duration.ofDays(1))
        .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()))
        .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())));
    return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory)
        .cacheDefaults(defaultCacheConfig)
        .build();
  }

  /**
   * Redis 값 직렬화/역직렬화를 위한 ObjectMapper
   * 1. visibility 설정
   *   - 어떤 값을 기준으로 직렬화/역직렬화 할지 설정가능하다. (생성자, getter, setter, Field 등)
   *   - 일관성 있는 직렬화/역직렬화를 위해 Field 만 사용하도록 설정한다.
   * 2. 날짜 타입을 LocalDateTime 타입으로 처리하기 위한 설정
   * 3. activeDefaultTyping 설정
   *   - 직렬화 시, 객체의 클래스를 Json 내에 명시해주는 설정이다.
   *   - ObjectMapper 가 Deserialize(readValue()) 할 때 Class 를 넘겨주어야 한다.
   *   - GenericJackson2JsonRedisSerializer 는 범용적인 타입에 쓰여야하므로, Json 내에 클래스가 명시되어 있어야 한다.
   *   - 클래스가 명시되지 않으면 객체는 기본으로 LinkedHashMap 으로 변환 하다가 오류가 발생하고, [] 의 경우, 리스트, 배열 등 타입이 모호하여 역직렬화시 에러가 발생한다.
   * @return
   */
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    PolymorphicTypeValidator ptv = mapper.getPolymorphicTypeValidator();

    return mapper
        .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(new JavaTimeModule())
        .activateDefaultTyping(ptv, DefaultTyping.EVERYTHING);
  }
}
