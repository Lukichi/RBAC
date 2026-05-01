package com.example.taxi.controller;

import com.example.taxi.entity.Passenger;
import com.example.taxi.entity.RequestData;
import com.example.taxi.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping()
    public ResponseEntity<List<Passenger>> getAll() {
        return ResponseEntity.ok(passengerService.getAll());
    }

    @PostMapping
    public ResponseEntity<Passenger> add(@RequestBody RequestData pr) {
        try {
            Passenger add = passengerService.add(pr.name(), pr.email(), pr.phone());
            return ResponseEntity.status(HttpStatus.CREATED).body(add);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Passenger> findById(@PathVariable Long id) {
        Passenger res = passengerService.findById(id).orElse(null);
        if (res == null)
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        return ResponseEntity.status(HttpStatus.FOUND).body(res);
    }
}
