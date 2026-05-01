package com.example.taxi.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ValueGenerationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "trips")
public class Trip {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(nullable = false)
    long passenger_id;
    @Column(nullable = false)
    long driver_id;
    String status;
    @Column(nullable = false)
    String origin;
    @Column(nullable = false)
    String destination;
    float price;
    String created_at;
    String updated_at;

    public Trip(){
    }

    public Trip(long pa_id, long dr_id, String start, String end, float price) {
        this.passenger_id = pa_id;
        this.driver_id = dr_id;
        this.origin = start;
        this.destination = end;
        this.price = price;
        this.created_at = LocalDateTime.now().format(DATE_TIME);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPassenger_id(long passenger_id) {
        this.passenger_id = passenger_id;
    }

    public void setDriver_id(long driver_id) {
        this.driver_id = driver_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public long getId() {
        return id;
    }

    public long getPassenger_id() {
        return passenger_id;
    }

    public long getDriver_id() {
        return driver_id;
    }

    public String getStatus() {
        return status;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public float getPrice() {
        return price;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }
}
