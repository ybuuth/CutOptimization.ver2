package com.example.cut_optimization.controller;

import com.example.cut_optimization.dto.ResultDataOptimization;
import com.example.cut_optimization.exception.CommonException;
import com.example.cut_optimization.optimizators.InitialDataOptimization;
import com.example.cut_optimization.service.OptimizeDispatcherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/optimization")
public class OptimizationController {

    private final ObjectMapper objectMapper;
    private final OptimizeDispatcherService optimizeDispatcherService;

    @Autowired
    public OptimizationController(ObjectMapper objectMapper, OptimizeDispatcherService optimizeDispatcherService) {
        this.objectMapper = objectMapper;
        this.optimizeDispatcherService = optimizeDispatcherService;
    }

    @PostMapping
    public ResponseEntity<ResultDataOptimization> optimization(@RequestBody String body) throws JsonProcessingException {
        log.info("body: {}", body);
        InitialDataOptimization initialDataOptimization = objectMapper.readValue(body, InitialDataOptimization.class);
        try {
            ResultDataOptimization resultDataOptimization = optimizeDispatcherService.optimize(initialDataOptimization);
            return ResponseEntity.ok(resultDataOptimization);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/postprocess")
    public ResponseEntity<ResultDataOptimization> postprocess(@RequestBody String body) throws JsonProcessingException {
        InitialDataOptimization initialDataOptimization = objectMapper.readValue(body, InitialDataOptimization.class);
        try {
            ResultDataOptimization resultDataOptimization = optimizeDispatcherService.postProcessOnly(initialDataOptimization);
            return ResponseEntity.ok(resultDataOptimization);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/enlarge")
    public ResponseEntity<ResultDataOptimization> enlarge (@RequestBody String body) throws JsonProcessingException {
        InitialDataOptimization initialDataOptimization = objectMapper.readValue(body, InitialDataOptimization.class);
        try {
            ResultDataOptimization resultDataOptimization = optimizeDispatcherService.enlarge(initialDataOptimization);
            return ResponseEntity.ok(resultDataOptimization);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public String ping() {
        return "pong";
    }
}
