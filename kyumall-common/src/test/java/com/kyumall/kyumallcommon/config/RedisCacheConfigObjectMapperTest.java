package com.kyumall.kyumallcommon.config;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class RedisCacheConfigObjectMapperTest {

  @Test
  void map_타입_Long_key_직렬화_테스트() throws JsonProcessingException {
    Map<Long, String> map = new HashMap<>();
    map.put(0L, "first");
    ObjectMapper objectMapper = new ObjectMapper();

    String serialized = objectMapper.writeValueAsString(map);

    log.info("serialized: {}", serialized);

    Map<Long, String> deserialized = objectMapper.readValue(serialized, Map.class);
    log.info("deserialized: {}", deserialized);

    String s = deserialized.get(0L);
    assertThat(s).isNull();     // Long 타입 Key 아님
    System.out.println(s);
  }



  @Test
  void map_key_type_Long_deserialize_test() throws JsonProcessingException {
    Map<Long, String> map = new HashMap<>();
    map.put(0L, "correct value");
    ObjectMapper objectMapper = customObjectMapper();     // objectMapper with setting defaultTyping

    String serialized = objectMapper.writeValueAsString(map);
    System.out.println("serialized: " + serialized);      // serialized: ["java.util.HashMap",{"0":"first"}]

    Map<Long, String> deserializedMap = objectMapper.readValue(serialized, Map.class);
    System.out.println("deserialized: " + deserializedMap);   // deserialized: {0=first}

    // check key type is not Long type
    String value1 = deserializedMap.get(0);
    assertThat(value1).isNull();
    System.out.println(value1);  // null
    // check key type is String
    String valueByStringKey = deserializedMap.get("0");
    assertThat(valueByStringKey).isEqualTo("correct value");
    System.out.println(valueByStringKey);  // correct value

  }

  public ObjectMapper customObjectMapper() {    // custom
    ObjectMapper mapper = new ObjectMapper();
    PolymorphicTypeValidator ptv = mapper.getPolymorphicTypeValidator();

    return mapper
        .activateDefaultTyping(ptv, DefaultTyping.EVERYTHING);    // typing everything
  }
}
