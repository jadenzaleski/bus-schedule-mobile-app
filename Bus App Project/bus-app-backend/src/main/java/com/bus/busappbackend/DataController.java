package com.bus.busappbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    @GetMapping("/data")
    public Map<String, String> getData() {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("message", "Hello, this is your JSON data.");
        return responseData;
    }
}
