package com.example.prManagement.repository;

import com.example.prManagement.model.Printer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrinterRepository extends JpaRepository<Printer, Long> {

    List<Printer> findByNameContainingIgnoreCaseAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(
            String name, Integer minToner, Integer minPages);

    List<Printer> findByNameContainingIgnoreCase(String name);

    List<Printer> findByTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(
            Integer minToner, Integer minPages);

    List<Printer> findByTonerLevelGreaterThanEqual(Integer minToner);

    List<Printer> findByPageCountGreaterThanEqual(Integer minPages);

    List<Printer> findByNameContainingIgnoreCaseAndTonerLevelGreaterThanEqual(String name, Integer minToner);
    List<Printer> findByNameContainingIgnoreCaseAndPageCountGreaterThanEqual(String name, Integer minPages);


    // Search by IP Address (partial match)
    List<Printer> findByIpAddressContaining(String ipAddress);

    // Combinations including IP Address search
    List<Printer> findByNameContainingIgnoreCaseAndIpAddressContaining(String name, String ipAddress);
    List<Printer> findByIpAddressContainingAndTonerLevelGreaterThanEqual(String ipAddress, Integer minToner);
    List<Printer> findByIpAddressContainingAndPageCountGreaterThanEqual(String ipAddress, Integer minPages);

    List<Printer> findByNameContainingIgnoreCaseAndIpAddressContainingAndTonerLevelGreaterThanEqual(
            String name, String ipAddress, Integer minToner);
    List<Printer> findByNameContainingIgnoreCaseAndIpAddressContainingAndPageCountGreaterThanEqual(
            String name, String ipAddress, Integer minPages);
    List<Printer> findByIpAddressContainingAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(
            String ipAddress, Integer minToner, Integer minPages);
    List<Printer> findByNameContainingIgnoreCaseAndIpAddressContainingAndTonerLevelGreaterThanEqualAndPageCountGreaterThanEqual(
            String name, String ipAddress, Integer minToner, Integer minPages);

}