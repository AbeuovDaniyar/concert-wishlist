package com.teamrhythm.concertwishlist.controller;

import com.teamrhythm.concertwishlist.dto.AttendedConcertDto;
import com.teamrhythm.concertwishlist.entity.AttendedConcert;
import com.teamrhythm.concertwishlist.entity.User;
import com.teamrhythm.concertwishlist.service.AttendedConcertService;
import com.teamrhythm.concertwishlist.service.AuthenticatedUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/attended")
public class AttendedController {

    private static final Logger log = LoggerFactory.getLogger(AttendedController.class);

    private final AttendedConcertService attendedConcertService;
    private final AuthenticatedUserService authenticatedUserService;

    @Autowired
    public AttendedController(AttendedConcertService attendedConcertService,
                              AuthenticatedUserService authenticatedUserService) {
        this.attendedConcertService = attendedConcertService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public String attendedConcerts(Model model, Authentication authentication) {
        Optional<User> userOptional = authenticatedUserService.findUser(authentication);
        if (userOptional.isEmpty()) {
            log.warn("Unable to resolve user for attended concerts page");
            return "redirect:/login";
        }

        Long userId = userOptional.get().getId();
        log.debug("Fetching attended concerts for user {}", userId);
        List<AttendedConcert> attended = attendedConcertService.getUserAttendedConcerts(userId);
        log.debug("Retrieved {} attended concerts for user {}", attended.size(), userId);
        model.addAttribute("attendedConcerts", attended);
        return "attended";
    }

    @GetMapping("/add")
    public String markAttendedForm(@RequestParam Long wishlistId,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = authenticatedUserService.findUser(authentication);
        if (userOptional.isEmpty()) {
            log.warn("Unable to resolve user for mark attended form");
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to mark concerts as attended.");
            return "redirect:/login";
        }

        log.debug("Rendering mark attended form for wishlist {}", wishlistId);
        AttendedConcertDto dto = new AttendedConcertDto();
        dto.setWishlistId(wishlistId);
        model.addAttribute("attendedDto", dto);
        return "mark-attended";
    }

    @PostMapping("/add")
    public String markAsAttended(@Valid @ModelAttribute("attendedDto") AttendedConcertDto dto,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            log.warn("Validation errors while marking wishlist {} as attended", dto.getWishlistId());
            return "mark-attended";
        }

        Optional<User> userOptional = authenticatedUserService.findUser(authentication);
        if (userOptional.isEmpty()) {
            log.warn("Unable to resolve user while marking concert as attended");
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to mark concerts as attended.");
            return "redirect:/login";
        }

        Long userId = userOptional.get().getId();
        try {
            log.info("Marking wishlist {} as attended for user {}", dto.getWishlistId(), userId);
            attendedConcertService.markAsAttended(userId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Concert marked as attended!");
            log.info("Wishlist {} marked as attended for user {}", dto.getWishlistId(), userId);
        } catch (RuntimeException e) {
            log.warn("Failed to mark wishlist {} as attended for user {}", dto.getWishlistId(), userId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/attended";
    }
}
