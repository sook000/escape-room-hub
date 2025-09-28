package cs.escaperoomhub.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisAvailabilityService {
    private final StringRedisTemplate redisTemplate;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private static String generateKey(Long timeslotId){
        return "timeslot-available::" + timeslotId;
    }

    // true/false/nullable(null이면 캐시 미스)
    public Boolean isAvailable(Long timeslotId) {
        try {
            String value = redisTemplate.opsForValue().get(generateKey(timeslotId));
            if (value == null) {
                return null;
            }
            return "1".equals(value);
        } catch (Exception e) {
            log.warn("Redis 가용성 조회 실패: timeslotId={}", timeslotId, e);
            return null; // Redis 오류 시 DB 조회
        }
    }

    public void setAvailable(Long timeslotId, boolean available) {
        setAvailable(timeslotId, available, DEFAULT_TTL);
    }

    public void setAvailable(Long timeslotId, boolean available, Duration ttl) {
        String key = generateKey(timeslotId);
        String value = available ? "1" : "0";
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    public void del(Long timeslotId) {
        redisTemplate.delete(generateKey(timeslotId));
    }
}