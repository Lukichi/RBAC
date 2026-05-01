package com.example.taxi.service;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.Passenger;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class DriverService {

    private static final Pattern UN_PATTER = Pattern.compile("^[a-zA-Z0-9_]{3,100}$");
    private static final Pattern EM_PATTER = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final Pattern PN_PATTER = Pattern.compile("^[0-9]+$");
    private static final Pattern ST_PATTER = Pattern.compile("^[A-Z]+$");

    @Autowired
    private final DriverRepository driverRepository;

    public DriverService(DriverRepository pr) {
        this.driverRepository = pr;
    }

    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    public Driver add(String name, String email, String phone) {
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

        return driverRepository.save(new Driver(name, email, phone));
    }

    public Optional<Driver> findById(long num) {
        return driverRepository.findById(num);
    }

    public Driver update(long id, String status) {
        status = status.trim().toUpperCase();
        if (!ST_PATTER.matcher((status)).matches() || status.isEmpty())
            return null;

        Driver res = driverRepository.findById(id).orElse(null);
        if (res == null)
            return null;

        res.setStatus(status);
        return driverRepository.save(res);
    }
}
