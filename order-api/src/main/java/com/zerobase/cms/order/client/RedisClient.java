package com.zerobase.cms.order.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static com.zerobase.cms.order.exception.ErrorCode.CART_CHANGE_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisClient {
    private final RedisTemplate<String, Object> redisTemplate;  // RedisConfig에 설정을 마친 상태.
    private static final ObjectMapper mapper = new ObjectMapper();

    public <T> T get(Long key, Class<T> classType) {
        return get(key.toString(), classType);
    }

    public <T> T get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);
        if (ObjectUtils.isEmpty(redisValue)) {  // 키에 대한 value값이 없는 경우
            return null;
        } else {    // value 값이 존재하는 경우
            try{
                // ObjectMapper 로 가져온 value 값을 클래스로 변환(파싱)
                return mapper.readValue(redisValue, classType);
            } catch (JsonProcessingException e) {
                log.error("Parsing Error", e);
                return null;
            }
        }
    }

    public void put(Long key, Cart cart) {
        put(key.toString(), cart);
    }

    public void put(String key, Cart cart) {
        try{
            // opsForValue() : Stings를 쉽게 serialize/deserialize 해주는 interface
            // ObjectMapper의 writeValueAsString(): 해당 객체를 json으로 변환
             redisTemplate.opsForValue().set(key, mapper.writeValueAsString(cart));
        } catch (JsonProcessingException e) {
            throw new CustomException(CART_CHANGE_ERROR);
        }
    }
}
