package dev.adi.customimpl.ratelimiter.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class RateLimitController {
    @GetMapping("/test-rate-limit")
    public ResponseEntity<String> getMethodName(@RequestParam("type") String param) {
        return ResponseEntity.ok("Succesfull " + param);
    }
    
}
