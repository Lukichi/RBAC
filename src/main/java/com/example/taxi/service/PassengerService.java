package com.example.taxi.service;

import com.example.taxi.entity.Passenger;
import com.example.taxi.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PassengerService {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,100}$");
    private static final Pattern EM_PATTER = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final Pattern PN_PATTER = Pattern.compile("^[0-9]+$");

    @Autowired
    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository pr) {
        this.passengerRepository = pr;
    }

    public List<Passenger> getAll() {
        return passengerRepository.findAll();
    }

    public Passenger add(String name, String email, String phone) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name not be empty");
        }
        if (!UN_PATTER.matcher((name)).matches()){
            throw new IllegalArgumentException("Invalid format name");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not be empty");
        }
        if (!EM_PATTER.matcher((email)).matches()){
            throw new IllegalArgumentException("Invalid format email");
        }

        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("Phone not be empty");
        }
        phone = phone.replaceAll(" ", "");
        phone = phone.substring(phone.length() - 10);
        if (!PN_PATTER.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid format phone");
        }

        return passengerRepository.save(new Passenger(name, email, phone));
    }

    public Optional<Passenger> findById(long num) {
        return passengerRepository.findById(num);
    }
}
