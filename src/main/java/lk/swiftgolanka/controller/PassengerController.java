package lk.swiftgolanka.controller;

import jakarta.validation.Valid;
import lk.swiftgolanka.dto.*;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.PaymentMethod;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/passenger")
public class PassengerController {

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

    @GetMapping("/dashboard")
    public String passengerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User passenger = userDetails.getUser();

        List<RideBooking> activeBookings = bookingService.getPassengerActiveBookings(passenger.getId());
        List<RideBooking> bookingHistory = bookingService.getPassengerBookings(passenger.getId());
        Optional<Trip> activeTrip = tripService.getActiveTripForPassenger(passenger.getId());
        List<Trip> tripHistory = tripService.getPassengerTripHistory(passenger.getId());
        List<Payment> payments = paymentService.getPassengerPayments(passenger.getId());
        List<Complaint> complaints = reportFeedbackService.getUserComplaints(passenger.getId());
        List<Notification> notifications = notificationService.getUserNotifications(passenger.getId());

        model.addAttribute("passenger", passenger);
        model.addAttribute("bookingRequestDTO", new BookingRequestDTO());
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("bookingHistory", bookingHistory);
        model.addAttribute("activeTrip", activeTrip.orElse(null));
        model.addAttribute("tripHistory", tripHistory);
        model.addAttribute("payments", payments);
        model.addAttribute("complaints", complaints);
        model.addAttribute("notifications", notifications);
        model.addAttribute("feedbackDTO", new FeedbackDTO());
        model.addAttribute("complaintDTO", new ComplaintDTO());

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
                                 RedirectAttributes redirectAttributes) {
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
                                @Valid @ModelAttribute RefundRequestDTO dto,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.requestRefund(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Refund request submitted to Finance Officer.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute FeedbackDTO dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.submitFeedback(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your rating and feedback!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/complaint")
    public String submitComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @Valid @ModelAttribute ComplaintDTO dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.submitComplaint(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint ticket logged with Customer Support.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/dashboard";
    }

    @PostMapping("/api/fare-estimate")
    @ResponseBody
    public ResponseEntity<FareEstimateResponseDTO> getFareEstimateApi(@RequestBody FareEstimateRequestDTO req) {
        return ResponseEntity.ok(mapService.calculateFareEstimate(req));
    }
}
