package com.example.taxi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/test")
    public String sayTest() {
        String phone = "+7 888 999 00 11";
        phone = phone.replaceAll("\\s", "");

        String sub = phone.substring(phone.length() - 10);
        return sub;
    }

}

