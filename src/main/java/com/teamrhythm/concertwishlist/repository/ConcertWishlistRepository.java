package com.teamrhythm.concertwishlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.teamrhythm.concertwishlist.entity.ConcertWishlist;

@Repository
public interface ConcertWishlistRepository extends JpaRepository<ConcertWishlist, Long> {
    List<ConcertWishlist> findByUserIdOrderByPriorityDesc(Long userId);
    List<ConcertWishlist> findByUserIdOrderByTargetDateAsc(Long userId);
    List<ConcertWishlist> findByUserIdAndStatus(Long userId, ConcertWishlist.Status status);
    Optional<ConcertWishlist> findByUserIdAndArtistIdAndCity(Long userId, Long artistId, String city);
    
    @Query("SELECT w FROM ConcertWishlist w WHERE w.user.id = :userId AND w.status = :status ORDER BY w.priority DESC, w.createdAt DESC")
    List<ConcertWishlist> findByUserIdAndStatusOrderByPriorityAndCreatedAt(@Param("userId") Long userId, @Param("status") ConcertWishlist.Status status);
}