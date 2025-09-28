package com.teamrhythm.concertwishlist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamrhythm.concertwishlist.entity.AttendedConcert;

@Repository
public interface AttendedConcertRepository extends JpaRepository<AttendedConcert, Long> {
    List<AttendedConcert> findByUserIdOrderByConcertDateDesc(Long userId);
    List<AttendedConcert> findByUserIdAndArtistId(Long userId, Long artistId);
    long countByUserId(Long userId);
}