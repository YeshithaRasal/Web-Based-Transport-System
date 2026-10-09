package lk.swiftgolanka.controller;

import jakarta.validation.Valid;
import lk.swiftgolanka.dto.*;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.BookingStatus;
import lk.swiftgolanka.enums.PaymentMethod;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/passenger")
public class PassengerController {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MapService mapService;

    @Autowired
    private DriverAssignmentService assignmentService;

    @GetMapping("/dashboard")
    public String passengerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User passenger = userDetails.getUser();

        List<RideBooking> activeBookings = bookingService.getPassengerActiveBookings(passenger.getId());
        List<RideBooking> bookingHistory = bookingService.getPassengerBookings(passenger.getId());
        Optional<Trip> activeTrip = tripService.getActiveTripForPassenger(passenger.getId());
        List<Trip> tripHistory = tripService.getPassengerTripHistory(passenger.getId());
        List<Payment> payments = paymentService.getPassengerPayments(passenger.getId());
        List<Refund> refunds = paymentService.getPassengerRefunds(passenger.getId());
        List<Complaint> complaints = reportFeedbackService.getUserComplaints(passenger.getId());
        List<Notification> notifications = notificationService.getUserNotifications(passenger.getId());

        List<Trip> unpaidTrips = paymentService.getUnpaidCompletedTripsForPassenger(passenger.getId());
        Set<Long> paidTripIds = paymentService.getPaidTripIdsForPassenger(passenger.getId());
        List<Trip> unreviewedTrips = reportFeedbackService.getUnreviewedCompletedTripsForPassenger(passenger.getId());
        Set<Long> reviewedTripIds = reportFeedbackService.getReviewedTripIdsForPassenger(passenger.getId());
        List<Feedback> passengerFeedbackList = reportFeedbackService.getPassengerFeedback(passenger.getId());

        Map<Long, List<DriverProfile>> bookingAvailableDrivers = new HashMap<>();
        Map<Long, DriverAssignment> latestAssignments = new HashMap<>();
        for (RideBooking b : activeBookings) {
            latestAssignments.put(b.getId(), assignmentService.getLatestAssignmentForBooking(b.getId()).orElse(null));
            if (b.getBookingStatus() == BookingStatus.REQUESTED || b.getBookingStatus() == BookingStatus.SEARCHING_DRIVER) {
                bookingAvailableDrivers.put(b.getId(), assignmentService.getAvailableDriversForCustomer(b.getVehicleType()));
            }
        }

        model.addAttribute("passenger", passenger);
        model.addAttribute("bookingRequestDTO", new BookingRequestDTO());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("bookingHistory", bookingHistory);
        model.addAttribute("activeTrip", activeTrip.orElse(null));
        model.addAttribute("tripHistory", tripHistory);
        model.addAttribute("payments", payments);
        model.addAttribute("refunds", refunds);
        model.addAttribute("complaints", complaints);
        model.addAttribute("notifications", notifications);
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        model.addAttribute("complaintDTO", new ComplaintDTO());

        model.addAttribute("unpaidTrips", unpaidTrips);
        model.addAttribute("paidTripIds", paidTripIds);
        model.addAttribute("unreviewedTrips", unreviewedTrips);
        model.addAttribute("reviewedTripIds", reviewedTripIds);
        model.addAttribute("passengerFeedbackList", passengerFeedbackList);
        model.addAttribute("bookingAvailableDrivers", bookingAvailableDrivers);
        model.addAttribute("latestAssignments", latestAssignments);

        return "passenger/dashboard";
    }

    @PostMapping("/book")
    public String createBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("bookingRequestDTO") BookingRequestDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide valid pickup, destination, and vehicle type.");
            return "redirect:/passenger/dashboard";
        }
        try {
            bookingService.createBooking(userDetails.getUser(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Ride booking created! Searching for available drivers...");
        } catch (InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/bookings/{id}/select-driver")
    public String selectDriver(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long bookingId,
                               @RequestParam("driverProfileId") Long driverProfileId,
                               RedirectAttributes redirectAttributes) {
        try {
            assignmentService.selectDriverForBooking(bookingId, driverProfileId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Driver requested! Waiting for driver response...");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/bookings/edit/{id}")
    public String updateBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("id") Long bookingId,
                                @ModelAttribute BookingRequestDTO dto,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBooking(bookingId, dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " route and details updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/bookings/cancel/{id}")
    public String cancelBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable("id") Long bookingId,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " has been cancelled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/payment/process")
    public String processPayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute PaymentRequestDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/passenger/dashboard";
        }
        try {
            Payment payment = paymentService.processPayment(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Payment of Rs. " + payment.getAmount() + " processed successfully! Ref: " + payment.getTransactionReference());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @GetMapping("/receipt/{paymentId}")
    public String viewReceipt(@PathVariable("paymentId") Long paymentId, Model model) {
        Payment payment = paymentService.getPaymentById(paymentId);
        model.addAttribute("payment", payment);
        return "passenger/receipt";
    }

    @PostMapping("/refund/request")
    public String requestRefund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("refundRequestDTO") RefundRequestDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid payment ID and reason for refund.");
            redirectAttributes.addFlashAttribute("activeTab", "payments");
            return "redirect:/passenger/dashboard";
        }
        try {
            paymentService.requestRefund(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Refund request submitted to Administration for review.");
            redirectAttributes.addFlashAttribute("activeTab", "payments");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "payments");
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute FeedbackDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid rating and comments.");
            redirectAttributes.addFlashAttribute("activeTab", "feedback");
            return "redirect:/passenger/dashboard";
        }
        try {
            reportFeedbackService.submitFeedback(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your rating and feedback!");
            redirectAttributes.addFlashAttribute("activeTab", "feedback");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "feedback");
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/complaint")
    public String submitComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @ModelAttribute("complaintDTO") ComplaintDTO dto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all required complaint fields (Subject, Category, Description).");
            redirectAttributes.addFlashAttribute("activeTab", "support");
            return "redirect:/passenger/dashboard";
        }
        try {
            reportFeedbackService.submitComplaint(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint ticket logged with Customer Support.");
            redirectAttributes.addFlashAttribute("activeTab", "support");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "support");
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/api/fare-estimate")
    @ResponseBody
    public ResponseEntity<FareEstimateResponseDTO> getFareEstimateApi(@RequestBody FareEstimateRequestDTO req) {
        return ResponseEntity.ok(mapService.calculateFareEstimate(req));
    }
}
