package com.example.taxi.controller;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.RequestData;
import com.example.taxi.service.DriverService;
import com.example.taxi.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping()
    public ResponseEntity<List<Driver>> getAll() {
        return ResponseEntity.ok(driverService.getAll());
    }

    @PostMapping
    public ResponseEntity<Driver> add(@RequestBody RequestData pr) {
        try {
            Driver add = driverService.add(pr.name(), pr.email(), pr.phone());
            return ResponseEntity.status(HttpStatus.CREATED).body(add);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> findById(@PathVariable Long id) {
        Driver res = driverService.findById(id).orElse(null);
        if (res == null)
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        return ResponseEntity.status(HttpStatus.FOUND).body(res);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Driver> updateById(@PathVariable Long id, @RequestParam String status) {
        Driver res = driverService.update(id, status);
        if (res == null)
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        return ResponseEntity.status(HttpStatus.FOUND).body(res);
    }
}
