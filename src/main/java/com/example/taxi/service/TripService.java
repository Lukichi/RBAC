package com.example.taxi.service;

import com.example.taxi.entity.Driver;
import com.example.taxi.entity.Passenger;
import com.example.taxi.entity.Trip;
import com.example.taxi.repository.DriverRepository;
import com.example.taxi.repository.PassengerRepository;
import com.example.taxi.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class TripService {

    private static final Pattern ST_PATTER = Pattern.compile("^[A-Z]+$");

    private final List<Integer> priceList = List.of(25, 30);

    @Autowired
    private final TripRepository tripRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public TripService(TripRepository tr, PassengerRepository passengerRepository, DriverRepository driverRepository) {
        this.tripRepository = tr;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Trip add(long pa_id, String start, String end, float length, int hour) {
        float price;
        if (length < 0) {
            throw new IllegalArgumentException("Invalid length");
        }

        if (hour < 10 || hour > 20) {
            price = priceList.get(1) * length;
        }
        else {
            price = priceList.get(0) * length;
        }

        price = Math.round(price * 100) / 100.f;

        Passenger help = passengerRepository.findById(pa_id).orElse(null);
        if (help == null) {
            throw new IllegalArgumentException("Invalid passenger id");
        }

        if (tripRepository.findPassenger(help)) {
            throw new IllegalArgumentException("Invalid passenger already have trip");
        }

        start = start.trim().toUpperCase();
        end = end.trim().toLowerCase();

        if (start.isEmpty() || end.isEmpty()) {
            throw new IllegalArgumentException("Invalid route points");
        }

        long driverId = getFreeDriverId();
        System.out.println("SET driver ID = " + driverId);

        return tripRepository.save(new Trip (pa_id, driverId, start, end, price));
    }

    @Transactional
    protected long getFreeDriverId() {
        long driverId;
        List<Driver> driverList = driverRepository.findFreeDriverWithLock();

        if (driverList.isEmpty()) {
            driverId = 0;
        }
        else {
            Driver driver = driverList.get(0);
            driver.setStatus("BUSY");
            driverRepository.save(driver);
            driverId = driver.getId();
        }

        return driverId;
    }

    public List<Trip> getAll(){
        return  tripRepository.findAll();
    }

    public Trip findById(long id) {
        return tripRepository.findById(id).orElse(null);
    }

    public Trip update(long id, String status) {
        status = status.trim().toUpperCase();
        if (!ST_PATTER.matcher((status)).matches() || status.isEmpty())
            return null;

        Trip res = tripRepository.findById(id).orElse(null);
        if (res == null)
            return null;

        if (status.equals("FINISHED")) {
            Driver driver = driverRepository.findById(res.getDriver_id()).orElse(null);
            driver.setStatus("FREE");
            driverRepository.save(driver);
        }

        res.setStatus(status);
        return tripRepository.save(res);
    }

    public List<Trip> findAllById(long id) {
        return tripRepository.findAllById(id);
    }

}
