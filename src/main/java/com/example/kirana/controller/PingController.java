package com.example.kirana.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Ping controller.
 */
@RestController
public class PingController {

    /**
     * Ping string.
     *
     * @return the string
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
