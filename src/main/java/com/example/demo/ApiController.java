package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ApiController {

    @GetMapping("/text")
    public String getText() {
        return "Hello world";
    }

    @GetMapping("/numbers")
    public List<Integer> getNumbers() {
        return List.of(10, 20, 30, 40, 50);
    }
}
