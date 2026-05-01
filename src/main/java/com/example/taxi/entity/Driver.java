package com.example.taxi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "drivers")
public class Driver {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String name;
    private String email;

    @Column(nullable = false)
    private String phone;
    private String created_at;
    private String license_number;
    private String status;

    public Driver() {
    }

    public Driver(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.created_at = LocalDateTime.now().format(DATE_TIME);
        this.license_number = UUID.randomUUID().toString();
        this.status = "FREE";
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getLicense_number() {
        return license_number;
    }

    public String getStatus() {
        return status;
    }
}
