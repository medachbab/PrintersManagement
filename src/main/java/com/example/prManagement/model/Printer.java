package com.example.prManagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column; // Import Column
import java.time.LocalDateTime; // Import LocalDateTime

@Entity
public class Printer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    private String name;
    private Integer tonerLevel;
    private Integer pageCount;

    // New: Field to store the last refresh timestamp for this printer
    @Column(name = "last_refresh_time") // Optional: specifies the column name in DB
    private LocalDateTime lastRefreshTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTonerLevel() {
        return tonerLevel;
    }

    public void setTonerLevel(Integer tonerLevel) {
        this.tonerLevel = tonerLevel;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    // New: Getter and Setter for lastRefreshTime
    public LocalDateTime getLastRefreshTime() {
        return lastRefreshTime;
    }

    public void setLastRefreshTime(LocalDateTime lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }
}
/*
package com.example.prManagement.model;


import jakarta.persistence.*;

@Entity
@Table(name = "printer")
public class Printer {

    @Id
    @SequenceGenerator(
            name= "printer_sequence",
            sequenceName= "printer_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "printer_sequence")
    private Long id;

    private String ipAddress;

    private String name;
    private Integer tonerLevel;
    private Integer pageCount;

    // Getters and setters
    public Long getId() { return id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getTonerLevel() { return tonerLevel; }
    public void setTonerLevel(Integer tonerLevel) { this.tonerLevel = tonerLevel; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
}
*/
