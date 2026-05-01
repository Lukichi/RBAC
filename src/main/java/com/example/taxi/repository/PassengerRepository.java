package com.example.taxi.repository;

import com.example.taxi.entity.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface  PassengerRepository extends JpaRepository<Passenger, Long> {

}
