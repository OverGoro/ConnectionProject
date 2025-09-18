// BufferJsonDataController.java
package com.service.bufferjsondata;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.connection.processing.buffer.objects.json.model.BufferJsonDataBLM;
import com.connection.processing.buffer.objects.json.model.BufferJsonDataDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/buffer-json-data-service")
public class BufferJsonDataController {
    
    private final BufferJsonDataService bufferJsonDataService;

    @PostMapping("/data")
    public ResponseEntity<BufferJsonDataBLM> addJsonData(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody BufferJsonDataDTO jsonDataDTO) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Adding JSON data to buffer: {}", jsonDataDTO.getBufferUid());
        
        BufferJsonDataBLM jsonData = bufferJsonDataService.addJsonData(accessToken, jsonDataDTO);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/{dataUid}")
    public ResponseEntity<BufferJsonDataBLM> getJsonData(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID dataUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting JSON data: {}", dataUid);
        
        BufferJsonDataBLM jsonData = bufferJsonDataService.getJsonData(accessToken, dataUid);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}")
    public ResponseEntity<List<BufferJsonDataBLM>> getJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting JSON data for buffer: {}", bufferUid);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getJsonDataByBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/after")
    public ResponseEntity<List<BufferJsonDataBLM>> getJsonDataByBufferAndCreatedAfter(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting JSON data for buffer {} created after: {}", bufferUid, createdAfter);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getJsonDataByBufferAndCreatedAfter(accessToken, bufferUid, createdAfter);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/before")
    public ResponseEntity<List<BufferJsonDataBLM>> getJsonDataByBufferAndCreatedBefore(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting JSON data for buffer {} created before: {}", bufferUid, createdBefore);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getJsonDataByBufferAndCreatedBefore(accessToken, bufferUid, createdBefore);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/between")
    public ResponseEntity<List<BufferJsonDataBLM>> getJsonDataByBufferAndCreatedBetween(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting JSON data for buffer {} between {} and {}", bufferUid, startDate, endDate);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getJsonDataByBufferAndCreatedBetween(accessToken, bufferUid, startDate, endDate);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/newest")
    public ResponseEntity<BufferJsonDataBLM> getNewestJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting newest JSON data for buffer: {}", bufferUid);
        
        BufferJsonDataBLM jsonData = bufferJsonDataService.getNewestJsonDataByBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/oldest")
    public ResponseEntity<BufferJsonDataBLM> getOldestJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting oldest JSON data for buffer: {}", bufferUid);
        
        BufferJsonDataBLM jsonData = bufferJsonDataService.getOldestJsonDataByBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/newest/{limit}")
    public ResponseEntity<List<BufferJsonDataBLM>> getNewestJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @PathVariable int limit) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting {} newest JSON data for buffer: {}", limit, bufferUid);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getNewestJsonDataByBuffer(accessToken, bufferUid, limit);
        return ResponseEntity.ok(jsonData);
    }

    @GetMapping("/data/buffer/{bufferUid}/oldest/{limit}")
    public ResponseEntity<List<BufferJsonDataBLM>> getOldestJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid,
            @PathVariable int limit) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Getting {} oldest JSON data for buffer: {}", limit, bufferUid);
        
        List<BufferJsonDataBLM> jsonData = bufferJsonDataService.getOldestJsonDataByBuffer(accessToken, bufferUid, limit);
        return ResponseEntity.ok(jsonData);
    }

    @DeleteMapping("/data/{dataUid}")
    public ResponseEntity<Void> deleteJsonData(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID dataUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting JSON data: {}", dataUid);
        
        bufferJsonDataService.deleteJsonData(accessToken, dataUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/data/buffer/{bufferUid}")
    public ResponseEntity<Void> deleteJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting all JSON data for buffer: {}", bufferUid);
        
        bufferJsonDataService.deleteJsonDataByBuffer(accessToken, bufferUid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/data/old")
    public ResponseEntity<Void> deleteOldJsonData(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant olderThan) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Deleting JSON data older than: {}", olderThan);
        
        bufferJsonDataService.deleteOldJsonData(accessToken, olderThan);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/data/buffer/{bufferUid}/count")
    public ResponseEntity<Integer> countJsonDataByBuffer(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable UUID bufferUid) {
        
        String accessToken = extractToken(authorizationHeader);
        log.info("Counting JSON data for buffer: {}", bufferUid);
        
        int count = bufferJsonDataService.countJsonDataByBuffer(accessToken, bufferUid);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        log.info("Health check: status: OK, service: buffer-json-data-service, timestamp: {}", 
                System.currentTimeMillis());

        return ResponseEntity.ok().body(bufferJsonDataService.getHealthStatus());
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid authorization header");
        }
        return authorizationHeader.substring(7);
    }
}