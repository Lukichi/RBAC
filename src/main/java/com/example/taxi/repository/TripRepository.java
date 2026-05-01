package com.example.taxi.repository;

import com.example.taxi.entity.Passenger;
import com.example.taxi.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    default boolean findPassenger(Passenger value) {
        List<Trip> list = this.findAll();

        for(Trip trip : list) {
            if (value.getId() == trip.getPassenger_id() && !Objects.equals(trip.getStatus(), "FINISHED")) {
                return true;
            }
        }

        return false;
    }

    default List<Trip> findAllById(long id) {
        List<Trip> res = new ArrayList<>();
        List<Trip> listTrips = this.findAll();

        for (Trip value : listTrips) {
            if (value.getPassenger_id() == id) {
                res.add(value);
            }
        }

        return res;
    }
}
