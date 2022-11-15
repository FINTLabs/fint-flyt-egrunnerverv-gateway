package no.fintlabs.cache;//package no.fintlabs.cache;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JavaType;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import no.fintlabs.cache.exceptions.NoSuchCacheException;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/admin/data-service/cache") // TODO: 29/06/2022 Burde være dynamisk applikasjonsid i url-en
//public class FintCacheInfoController {
//
//    private final FintCacheManager fintCacheManager;
//    private final ObjectMapper objectMapper;
//
//    public FintCacheInfoController(FintCacheManager fintCacheManager, ObjectMapper objectMapper) {
//        this.fintCacheManager = fintCacheManager;
//        this.objectMapper = objectMapper;
//    }
//
//    @GetMapping("info")
//    public ResponseEntity<FintCacheInfo> getCacheInfo(
//            @RequestParam() String alias,
//            @RequestParam() String keyType,
//            @RequestParam() String valueType
//    ) {
//        try {
//            Class<?> keyClass = Class.forName(keyType);
//            Class<?> valueClass = Class.forName(valueType);
//
//            FintCache<?, ?> cache = fintCacheManager.getCache(alias, keyClass, valueClass);
//            FintCacheInfo cacheInfo = FintCacheInfo
//                    .builder()
//                    .alias(cache.getAlias())
//                    .numberOfEntries(cache.getNumberOfEntries())
//                    .numberOfDistinctEntries(cache.getNumberOfDistinctValues())
//                    .build();
//            return ResponseEntity.ok(cacheInfo);
//        } catch (NoSuchCacheException e) {
//            return ResponseEntity.notFound().build();
//        } catch (ClassNotFoundException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("value")
//    public ResponseEntity<?> getCacheValue(
//            @RequestParam() String alias,
//            @RequestParam() String keyType,
//            @RequestParam() String valueType,
//            @RequestParam() String key
//    ) {
//        try {
//            Class<?> keyClass = Class.forName(keyType);
//            JavaType keyJavaType = objectMapper.getTypeFactory().constructType(keyClass);
//            Class<?> valueClass = Class.forName(valueType);
//
//            FintCache<?, ?> cache = fintCacheManager.getCache(alias, keyClass, valueClass);
//            return cache
//                    .getOptional(objectMapper.readValue(key, keyJavaType))
//                    .map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (NoSuchCacheException e) {
//            return ResponseEntity.notFound().build();
//        } catch (ClassNotFoundException e) {
//            return ResponseEntity.badRequest().build();
//        } catch (JsonProcessingException e) {
//            return ResponseEntity.unprocessableEntity().build();
//        }
//    }
//
//    @GetMapping("values")
//    public ResponseEntity<List<?>> getCacheValues(
//            @RequestParam() String alias,
//            @RequestParam() String keyType,
//            @RequestParam() String valueType,
//            @RequestParam() Optional<Boolean> distinct
//    ) {
//        try {
//            Class<?> keyClass = Class.forName(keyType);
//            Class<?> valueClass = Class.forName(valueType);
//
//            FintCache<?, ?> cache = fintCacheManager.getCache(alias, keyClass, valueClass);
//            return ResponseEntity.ok(
//                    distinct.orElse(true) ? cache.getAllDistinct() : cache.getAll()
//            );
//        } catch (NoSuchCacheException e) {
//            return ResponseEntity.notFound().build();
//        } catch (ClassNotFoundException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//}
