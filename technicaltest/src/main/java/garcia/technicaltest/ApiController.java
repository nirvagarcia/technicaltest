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
    private static final String API_KEY = "b28ecf24b43e485db9e434850811e0f1";

    @PostMapping("/financialData")
    public ResponseEntity<String> storeFinancialData(@RequestBody FinancialRequest financialRequest) {
        String apiUrl = buildApiUrl(financialRequest);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(apiUrl, (Class<Map<String, Object>>)(Class<?>)Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> financialData = response.getBody();
            if (financialData != null) {
                String key = DATA_KEY_PREFIX + financialRequest.getType() + ":" + financialRequest.getSymbol();
                redisTemplate.opsForValue().set(key, financialData);
                return ResponseEntity.ok("Datos almacenados para: " + financialRequest.getType() + " " + financialRequest.getSymbol());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al almacenar los datos en Redis");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener datos del API");
        }
    }

    @GetMapping("/financialData")
    public ResponseEntity<Object> getFinancialData(@RequestParam String type, @RequestParam String symbol) {
        String key = DATA_KEY_PREFIX + type + ":" + symbol;
        Object financialData = redisTemplate.opsForValue().get(key);
        if (financialData != null) {
            return ResponseEntity.ok(financialData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }
    }

    @GetMapping("/financialData/summary")
    public ResponseEntity<Object> getFinancialDataSummary(@RequestParam String type, @RequestParam String symbol) {
        String key = DATA_KEY_PREFIX + type + ":" + symbol;
        Object financialData = redisTemplate.opsForValue().get(key);

        if (financialData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found");
        }

        if (type.equals("stock")) {
            Map<String, Object> data = (Map<String, Object>) financialData;
            if (data.containsKey("values")) {
                List<Map<String, String>> dataPoints = (List<Map<String, String>>) data.get("values");
                Map<String, Object> summary = calculateSummary(dataPoints);
                return ResponseEntity.ok(summary);
            }
        } else if (type.equals("forex")) {
            Map<String, Object> data = (Map<String, Object>) financialData;
            if (data.containsKey("rate")) {
                return ResponseEntity.ok(data);
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the data");
    }

    private Map<String, Object> calculateSummary(List<Map<String, String>> dataPoints) {
        double totalOpen = 0, totalClose = 0, totalHigh = 0, totalLow = 0;
        int count = dataPoints.size();

        for (Map<String, String> dataPoint : dataPoints) {
            totalOpen += Double.parseDouble(dataPoint.get("open"));
            totalClose += Double.parseDouble(dataPoint.get("close"));
            totalHigh += Double.parseDouble(dataPoint.get("high"));
            totalLow += Double.parseDouble(dataPoint.get("low"));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("averageOpen", totalOpen / count);
        summary.put("averageClose", totalClose / count);
        summary.put("averageHigh", totalHigh / count);
        summary.put("averageLow", totalLow / count);

        return summary;
    }

    private String buildApiUrl(FinancialRequest financialRequest) {
        String apiUrl = "https://api.twelvedata.com/";
        switch (financialRequest.getType()) {
            case "stock":
                apiUrl += "time_series?symbol=" + financialRequest.getSymbol() + "&interval=" + financialRequest.getInterval() + "&apikey=" + API_KEY;
                break;
            case "forex":
                apiUrl += "exchange_rate?symbol=" + financialRequest.getSymbol() + "&apikey=" + API_KEY;
                break;
            case "crypto":
                apiUrl += "time_series?symbol=" + financialRequest.getSymbol() + "&interval=" + financialRequest.getInterval() + "&apikey=" + API_KEY;
                break;
            case "etf":
                apiUrl += "quote?symbol=" + financialRequest.getSymbol() + "&apikey=" + API_KEY;
                break;
            case "index":
                apiUrl += "quote?symbol=" + financialRequest.getSymbol() + "&apikey=" + API_KEY;
                break;
            case "fund":
                apiUrl += "quote?symbol=" + financialRequest.getSymbol() + "&apikey=" + API_KEY;
                break;
        }
        return apiUrl;
    }
}