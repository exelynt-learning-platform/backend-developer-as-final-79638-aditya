package com.booking.service;

import com.booking.dto.PageResponse;
import com.booking.dto.ReservationRequest;
import com.booking.dto.ReservationResponse;
import com.booking.model.Reservation;
import com.booking.model.ReservationStatus;
import com.booking.model.Resource;
import com.booking.model.Role;
import com.booking.model.User;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.repository.spec.ReservationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationResponse createReservation(ReservationRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        User currentUser = getCurrentUser();
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        if (!resource.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource is currently inactive");
        }

        // Calculate total price based on hours (rounded up)
        long hours = Duration.between(request.getStartTime(), request.getEndTime()).toHours();
        if (hours == 0) hours = 1; // Minimum 1 hour charge
        BigDecimal totalPrice = resource.getPrice().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(currentUser)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .build();

        return mapToResponse(reservationRepository.save(reservation));
    }

    public PageResponse<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        User currentUser = getCurrentUser();

        // If ADMIN, userIdFilter is null (sees all). If USER, filter by their ID.
        Long userIdFilter = (currentUser.getRole() == Role.ADMIN) ? null : currentUser.getId();

        // Build the dynamic query
        Specification<Reservation> spec = ReservationSpecification.filterReservations(
                status, minPrice, maxPrice, userIdFilter
        );

        // Execute paginated and filtered query
        Page<Reservation> page = reservationRepository.findAll(spec, pageable);

        List<ReservationResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReservationResponse>builder()
                .content(content)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = findReservationOrThrow(id);
        verifyOwnershipOrAdmin(reservation, getCurrentUser());
        return mapToResponse(reservation);
    }

    public ReservationResponse updateReservationStatus(Long id, ReservationStatus status) {
        Reservation reservation = findReservationOrThrow(id);
        User currentUser = getCurrentUser();

        // USERs can only cancel their own reservations. ADMINs can do anything.
        if (currentUser.getRole() != Role.ADMIN) {
            verifyOwnershipOrAdmin(reservation, currentUser);
            if (status != ReservationStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users can only cancel reservations");
            }
        }

        reservation.setStatus(status);
        return mapToResponse(reservationRepository.save(reservation));
    }

    // --- Helper Methods ---

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));
    }

    private Reservation findReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    private void verifyOwnershipOrAdmin(Reservation reservation, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You do not own this reservation");
        }
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .userEmail(reservation.getUser().getEmail())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .build();
    }
}