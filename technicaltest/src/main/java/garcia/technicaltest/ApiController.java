package garcia.technicaltest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(apiUrl, (Class<Map<String, Object>>)(Class<?>)Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> financialData = response.getBody();
            if (financialData != null) {
                if (financialData.containsKey("Time Series (5min)")) {
                    Map<String, Object> timeSeriesData = (Map<String, Object>) financialData.get("Time Series (5min)");
                    for (Map.Entry<String, Object> timeEntry : timeSeriesData.entrySet()) {
                        String key = DATA_KEY_PREFIX + financialRequest.getSymbol() + ":" + timeEntry.getKey();
                        redisTemplate.opsForValue().set(key, timeEntry.getValue());
                    }
                }
            }
            return ResponseEntity.ok("Datos almacenados para: " + financialRequest.getType() + " " + financialRequest.getSymbol());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener datos del API");
        }
    }

    @GetMapping("/financialData")
    public ResponseEntity<Object> getFinancialData(@RequestParam String type, @RequestParam String symbol) {
        String keyPattern = DATA_KEY_PREFIX + symbol + ":*";
        Set<String> keys = redisTemplate.keys(keyPattern);

        if (keys == null || keys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }

        List<Map<String, String>> dataPoints = new ArrayList<>();
        for (String key : keys) {
            Map<String, String> data = (Map<String, String>) redisTemplate.opsForValue().get(key);
            dataPoints.add(data);
        }

        Map<String, Object> summary = calculateSummary(dataPoints);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/financialData/summary")
    public ResponseEntity<Object> getFinancialDataSummary(@RequestParam String type, @RequestParam(required = false) String symbol, @RequestParam(required = false) String fromCurrency, @RequestParam(required = false) String toCurrency) {
        String keyPattern;

        if ("stock".equalsIgnoreCase(type)) {
            keyPattern = DATA_KEY_PREFIX + symbol + ":*";
        } else if ("forex".equalsIgnoreCase(type)) {
            keyPattern = DATA_KEY_PREFIX + fromCurrency + toCurrency + ":*";
        } else {
            return ResponseEntity.badRequest().body("Invalid type");
        }

        Set<String> keys = redisTemplate.keys(keyPattern);

        if (keys == null || keys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }

        List<Map<String, String>> dataPoints = new ArrayList<>();
        for (String key : keys) {
            Map<String, String> data = (Map<String, String>) redisTemplate.opsForValue().get(key);
            dataPoints.add(data);
        }

        Map<String, Object> summary = calculateSummary(dataPoints);
        return ResponseEntity.ok(summary);
    }

    private Map<String, Object> calculateSummary(List<Map<String, String>> dataPoints) {
        double totalOpen = 0, totalClose = 0, totalHigh = 0, totalLow = 0;
        int count = dataPoints.size();

        for (Map<String, String> dataPoint : dataPoints) {
            totalOpen += Double.parseDouble(dataPoint.get("1. open"));
            totalClose += Double.parseDouble(dataPoint.get("4. close"));
            totalHigh += Double.parseDouble(dataPoint.get("2. high"));
            totalLow += Double.parseDouble(dataPoint.get("3. low"));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("averageOpen", totalOpen / count);
        summary.put("averageClose", totalClose / count);
        summary.put("averageHigh", totalHigh / count);
        summary.put("averageLow", totalLow / count);

        return summary;
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
        }
        return apiUrl;
    }
}