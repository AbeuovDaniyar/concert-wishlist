package com.teamrhythm.concertwishlist.service;

import com.teamrhythm.concertwishlist.dto.AttendedConcertDto;
import com.teamrhythm.concertwishlist.entity.AttendedConcert;
import com.teamrhythm.concertwishlist.entity.ConcertWishlist;
import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.repository.AttendedConcertRepository;
import com.teamrhythm.concertwishlist.repository.ConcertWishlistRepository;
import com.teamrhythm.concertwishlist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AttendedConcertService {

    private static final Logger log = LoggerFactory.getLogger(AttendedConcertService.class);

    private final AttendedConcertRepository attendedConcertRepository;
    private final ConcertWishlistRepository wishlistRepository;
    private final UserRepository userRepository;

    @Autowired
    public AttendedConcertService(AttendedConcertRepository attendedConcertRepository,
                                 ConcertWishlistRepository wishlistRepository,
                                 UserRepository userRepository) {
        this.attendedConcertRepository = attendedConcertRepository;
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AttendedConcert markAsAttended(Long userId, AttendedConcertDto dto) {
        log.info("Marking wishlist {} as attended for user {}", dto.getWishlistId(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User {} not found while marking attended concert", userId);
                    return new RuntimeException("User not found");
                });

        ConcertWishlist wishlist = wishlistRepository.findById(dto.getWishlistId())
                .orElseThrow(() -> {
                    log.warn("Wishlist item {} not found while marking attended", dto.getWishlistId());
                    return new RuntimeException("Wishlist item not found");
                });

        if (!wishlist.getUser().getId().equals(userId)) {
            log.warn("Unauthorized attempt to modify wishlist {} by user {}", wishlist.getId(), userId);
            throw new RuntimeException("Unauthorized to modify this wishlist item");
        }

        AttendedConcert attendedConcert = new AttendedConcert();
        attendedConcert.setUser(user);
        attendedConcert.setArtist(wishlist.getArtist());
        attendedConcert.setCity(wishlist.getCity());
        attendedConcert.setVenue(dto.getVenue());
        attendedConcert.setConcertDate(dto.getConcertDate());
        attendedConcert.setRating(dto.getRating());
        attendedConcert.setMemories(dto.getMemories());
        attendedConcert.setWishlistId(wishlist.getId());

        AttendedConcert saved = attendedConcertRepository.save(attendedConcert);
        log.info("Created attended concert record {} for user {}", saved.getId(), userId);

        wishlist.setStatus(ConcertWishlist.Status.ATTENDED);
        wishlistRepository.save(wishlist);
        log.debug("Updated wishlist {} status to ATTENDED", wishlist.getId());

        return saved;
    }

    public List<AttendedConcert> getUserAttendedConcerts(Long userId) {
        log.debug("Fetching attended concerts for user {}", userId);
        return attendedConcertRepository.findByUserIdOrderByConcertDateDesc(userId);
    }

    public long getUserAttendedCount(Long userId) {
        log.debug("Counting attended concerts for user {}", userId);
        return attendedConcertRepository.countByUserId(userId);
    }
}
