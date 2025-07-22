package com.example.prManagement.repository;


import com.example.prManagement.model.Printer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrinterRepository extends JpaRepository<Printer, Long> {
}
