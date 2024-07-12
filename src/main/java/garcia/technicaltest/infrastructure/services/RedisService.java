package garcia.technicaltest.infrastructure.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DATA_KEY_PREFIX = "financialData:";

    public Map<String, List<String>> getTypesAndSymbols() {
        Map<String, List<String>> options = new HashMap<>();
        Set<String> keys = redisTemplate.keys(DATA_KEY_PREFIX + "*");

        if (keys != null) {
            for (String key : keys) {
                String[] parts = key.split(":");
                if (parts.length == 3) {
                    String type = parts[1];
                    String symbol = parts[2];

                    options.putIfAbsent(type, new ArrayList<>());
                    options.get(type).add(symbol);
                }
            }
        }
        return options;
    }
}