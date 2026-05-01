package com.example.taxi.controller;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.RequetTrip;
import com.example.taxi.entity.Trip;
import com.example.taxi.service.PassengerService;
import com.example.taxi.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private final TripService tripService;

    public TripController(TripService ts) {
        this.tripService = ts;
    }

    @PostMapping
    public ResponseEntity<Trip> add(@RequestBody RequetTrip value) {
        try {
            Trip res = tripService.add(value.id(), value.start(), value.end(), value.length(), value.hour());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Trip>> getAll() {
        return ResponseEntity.ok().body(tripService.getAll());
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getAllById(@RequestParam long passenger_id) {
        return ResponseEntity.ok().body(tripService.findAllById(passenger_id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getById(@PathVariable long id) {
        Trip res = tripService.findById(id);
        if (res == null)
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        return ResponseEntity.status(HttpStatus.FOUND).body(res);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Trip> updateById(@PathVariable Long id, @RequestParam String status) {
        Trip res = tripService.update(id, status);
        if (res == null)
            ResponseEntity.status(HttpStatus.CONFLICT).body(null);

        return ResponseEntity.status(HttpStatus.FOUND).body(res);
    }

}
