package com.example.prManagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Printer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    private String name;
    private Integer tonerLevel;
    private Integer pageCount;
    private LocalDateTime lastRefreshTime;


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

    public LocalDateTime getLastRefreshTime() {
        return lastRefreshTime;
    }

    public void setLastRefreshTime(LocalDateTime lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    @Override
    public String toString() {
        return "Printer{" +
                "id=" + id +
                ", ipAddress='" + ipAddress + '\'' +
                ", name='" + name + '\'' +
                ", tonerLevel=" + tonerLevel +
                ", pageCount=" + pageCount +
                ", lastRefreshTime=" + lastRefreshTime +
                '}';
    }
}