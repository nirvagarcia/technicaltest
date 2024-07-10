package garcia.technicaltest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DATA_KEY_PREFIX = "financialData:";
    private static final String API_KEY = "D0BB06WI0ONDDW4C";

    @PostMapping("/financialData")
    public ResponseEntity<String> storeFinancialData(@RequestBody FinancialRequest financialRequest) {
        String apiUrl = buildApiUrl(financialRequest);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> financialData = response.getBody();
            if (financialData != null) {
                for (Map.Entry<String, Object> entry : financialData.entrySet()) {
                    if (entry.getKey().equals("Time Series (5min)")) {
                        Map<String, Object> timeSeriesData = (Map<String, Object>) entry.getValue();
                        for (Map.Entry<String, Object> timeEntry : timeSeriesData.entrySet()) {
                            String key = DATA_KEY_PREFIX + financialRequest.getSymbol() + ":" + timeEntry.getKey();
                            redisTemplate.opsForValue().set(key, timeEntry.getValue());
                        }
                    }
                }
            }
            return ResponseEntity.ok("Datos almacenados para: " + financialRequest.getType() + " " + financialRequest.getSymbol());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener datos del API");
        }
    }

    @GetMapping("/financialData")
    public ResponseEntity<Object> getFinancialData(@RequestParam String symbol, @RequestParam String timestamp) {
        String key = DATA_KEY_PREFIX + symbol + ":" + timestamp;
        Object financialData = redisTemplate.opsForValue().get(key);
        if (financialData != null) {
            return ResponseEntity.ok(financialData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }
    }

    private String buildApiUrl(FinancialRequest financialRequest) {
        String apiUrl = "";
        switch (financialRequest.getType()) {
            case "stock":
                apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="
                         + financialRequest.getSymbol() + "&interval=5min&apikey=" + API_KEY;
                break;
            case "forex":
                apiUrl = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency="
                         + financialRequest.getFromCurrency() + "&to_currency=" + financialRequest.getToCurrency() + "&apikey=" + API_KEY;
                break;
            // Añadir otros casos según sea necesario
        }
        return apiUrl;
    }
}
