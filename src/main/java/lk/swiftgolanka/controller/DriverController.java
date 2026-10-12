package lk.swiftgolanka.controller;

import lk.swiftgolanka.dto.ComplaintDTO;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.TripStatus;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/driver")
public class DriverController {

    @Autowired
    private UserService userService;

    @Autowired
    private DriverAssignmentService assignmentService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String driverDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User driverUser = userDetails.getUser();
        DriverProfile driverProfile = userService.getDriverProfileByUserId(driverUser.getId());

        List<DriverAssignment> pendingAssignments = assignmentService.getPendingAssignmentsForDriver(driverUser.getId());
        Optional<Trip> activeTrip = tripService.getActiveTripForDriver(driverUser.getId());
        List<Trip> tripHistory = tripService.getDriverTripHistory(driverUser.getId());
        List<DriverCommission> commissions = paymentService.getDriverCommissions(driverUser.getId());
        Double totalEarnings = paymentService.getDriverTotalEarnings(driverUser.getId());
        Double totalIncome = paymentService.getDriverTotalIncome(driverUser.getId());
        List<Payment> driverPayments = paymentService.getDriverPayments(driverUser.getId());
        long totalPaidTrips = paymentService.countPaidTripsForDriver(driverProfile.getId());
        List<Feedback> feedbackList = reportFeedbackService.getDriverFeedback(driverUser.getId());
        List<Notification> notifications = notificationService.getUserNotifications(driverUser.getId());
        Set<Long> paidTripIds = paymentService.getPaidTripIdsForDriver(driverUser.getId());

        model.addAttribute("driverUser", driverUser);
        model.addAttribute("driverProfile", driverProfile);
        model.addAttribute("pendingAssignments", pendingAssignments);
        model.addAttribute("activeTrip", activeTrip.orElse(null));
        model.addAttribute("tripHistory", tripHistory);
        model.addAttribute("commissions", commissions);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("driverPayments", driverPayments);
        model.addAttribute("totalPaidTrips", totalPaidTrips);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("notifications", notifications);
        model.addAttribute("complaintDTO", new ComplaintDTO());
        model.addAttribute("paidTripIds", paidTripIds);

        return "driver/dashboard";
    }

    @PostMapping("/availability")
    public String toggleAvailability(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestParam("status") AvailabilityStatus status,
                                     RedirectAttributes redirectAttributes) {
        try {
            assignmentService.toggleDriverAvailability(userDetails.getUser().getId(), status);
            redirectAttributes.addFlashAttribute("successMessage", "Availability status updated to: " + status.name());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    @PostMapping("/assignments/accept/{id}")
    public String acceptAssignment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable("id") Long assignmentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            assignmentService.driverAcceptAssignment(assignmentId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Ride request accepted! Trip created.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    @PostMapping("/assignments/reject/{id}")
    public String rejectAssignment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable("id") Long assignmentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            assignmentService.driverRejectAssignment(assignmentId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Ride request declined.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    @PostMapping("/trip/update-status")
    public String updateTripStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestParam("tripId") Long tripId,
                                   @RequestParam("status") TripStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            tripService.updateTripStatus(tripId, status, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Trip status updated to: " + status.name());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    @PostMapping("/complaint")
    public String submitComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @ModelAttribute ComplaintDTO dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.submitComplaint(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint ticket logged with Customer Support.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }

    @PostMapping("/trip/cash-payment/{tripId}")
    public String confirmCashPayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("tripId") Long tripId,
                                     RedirectAttributes redirectAttributes) {
        try {
            paymentService.processCashPaymentByDriver(tripId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Cash payment confirmed! Earnings credited to your ledger.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/driver/dashboard";
    }
}
