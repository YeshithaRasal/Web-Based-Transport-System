package lk.swiftgolanka.controller;

import jakarta.validation.Valid;
import lk.swiftgolanka.dto.*;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.ComplaintStatus;
import lk.swiftgolanka.enums.ReportType;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.enums.TripStatus;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @Autowired
    private DriverAssignmentService assignmentService;

    @GetMapping("/dashboard")
    public String adminDashboard(@RequestParam(value = "query", required = false) String query,
                                 @RequestParam(value = "roleFilter", required = false) Role roleFilter,
                                 @RequestParam(value = "viewReportId", required = false) Long viewReportId,
                                 Model model) {
        List<User> users = userService.searchUsers(query, roleFilter);
        List<DriverProfile> drivers = userService.getAllDrivers();
        List<RideBooking> bookings = bookingService.getAllRecentBookings();
        List<Trip> trips = tripService.getAllTrips();
        List<Payment> payments = paymentService.getAllPayments();
        List<Refund> refunds = paymentService.getAllRefunds();
        List<Feedback> feedbackList = reportFeedbackService.getAllFeedback();
        List<Complaint> complaints = reportFeedbackService.getAllComplaints();
        List<Report> reports = reportFeedbackService.getAllReports();
        List<DriverAssignment> assignments = assignmentService.getAllAssignments();

        List<DriverIncomeSummaryDTO> driverIncomeSummaries = new ArrayList<>();
        for (DriverProfile d : drivers) {
            Long paidTrips = paymentService.countPaidTripsForDriver(d.getId());
            Double income = paymentService.getDriverTotalIncomeByProfileId(d.getId());
            driverIncomeSummaries.add(new DriverIncomeSummaryDTO(
                    d.getId(),
                    d.getUser().getFullName(),
                    d.getVehicleNumber(),
                    d.getVehicleType().name(),
                    paidTrips != null ? paidTrips : 0L,
                    income != null ? income : 0.0
            ));
        }

        if (viewReportId != null) {
            try {
                GeneratedReportDTO activeReport = reportFeedbackService.getReportDataById(viewReportId);
                model.addAttribute("activeReport", activeReport);
            } catch (Exception ignored) {}
        }

        model.addAttribute("users", users);
        model.addAttribute("drivers", drivers);
        model.addAttribute("bookings", bookings);
        model.addAttribute("trips", trips);
        model.addAttribute("payments", payments);
        model.addAttribute("refunds", refunds);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("complaints", complaints);
        model.addAttribute("reports", reports);
        model.addAttribute("assignments", assignments);
        model.addAttribute("driverIncomeSummaries", driverIncomeSummaries);
        model.addAttribute("staffDTO", new StaffRegisterDTO());
        model.addAttribute("reportRequestDTO", new ReportRequestDTO());
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("roles", Role.values());
        model.addAttribute("query", query);
        model.addAttribute("roleFilter", roleFilter);

        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("platformFees", paymentService.getTotalPlatformFees());

        return "admin/dashboard";
    }

    @PostMapping("/users/create-staff")
    public String createStaffAccount(@Valid @ModelAttribute("staffDTO") StaffRegisterDTO dto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide valid details for staff user creation.");
            return "redirect:/admin/dashboard";
        }
        try {
            authService.createStaffUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Staff user (" + dto.getRole().name() + ") created successfully for " + dto.getEmail());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/drivers/verify/{id}")
    public String verifyDriver(@PathVariable("id") Long driverProfileId,
                               @RequestParam("approve") boolean approve,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.verifyDriver(driverProfileId, approve);
            redirectAttributes.addFlashAttribute("successMessage", "Driver verification updated (" + (approve ? "APPROVED" : "REJECTED") + ").");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleAccountStatus(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User account status toggled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.softDeleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User soft-deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/bookings/cancel/{id}")
    public String adminCancelBooking(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("id") Long bookingId,
                                     RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + bookingId + " cancelled by Admin.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/assignments/cancel/{id}")
    public String adminCancelAssignment(@PathVariable("id") Long assignmentId,
                                        RedirectAttributes redirectAttributes) {
        try {
            assignmentService.adminCancelAssignment(assignmentId);
            redirectAttributes.addFlashAttribute("successMessage", "Driver assignment #" + assignmentId + " cancelled.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/trips/update-status/{id}")
    public String adminUpdateTripStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("id") Long tripId,
                                        @RequestParam("status") TripStatus status,
                                        RedirectAttributes redirectAttributes) {
        try {
            tripService.adminUpdateTripStatus(tripId, status, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Trip #" + tripId + " status changed to " + status.name());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/trips/cancel/{id}")
    public String adminCancelTrip(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long tripId,
                                  RedirectAttributes redirectAttributes) {
        try {
            tripService.adminCancelTrip(tripId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Trip #" + tripId + " cancelled by Administrator.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/trips/delete/{id}")
    public String adminDeleteTrip(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long tripId,
                                  RedirectAttributes redirectAttributes) {
        try {
            tripService.adminDeleteTrip(tripId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Trip #" + tripId + " removed from records.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/payments/void/{id}")
    public String voidPayment(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable("id") Long paymentId,
                              RedirectAttributes redirectAttributes) {
        try {
            paymentService.voidPayment(paymentId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Payment #" + paymentId + " marked as VOID / CANCELLED.");
            redirectAttributes.addFlashAttribute("activeTab", "finance");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "finance");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/refunds/process")
    public String processRefund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("refundId") Long refundId,
                                @RequestParam("approve") boolean approve,
                                @RequestParam(value = "remarks", required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.processRefund(refundId, approve, remarks, userDetails.getUser());
            String actionMsg = approve ? "APPROVED & PROCESSED successfully (Fair reason accepted)." : "REJECTED.";
            redirectAttributes.addFlashAttribute("successMessage", "Refund claim #" + refundId + " has been " + actionMsg);
            redirectAttributes.addFlashAttribute("activeTab", "finance");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "finance");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/complaints/{id}/status")
    public String updateComplaintStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("id") Long complaintId,
                                        @RequestParam("status") ComplaintStatus status,
                                        @RequestParam(value = "resolutionNotes", required = false) String resolutionNotes,
                                        RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.updateComplaintStatus(complaintId, status, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint #" + complaintId + " status updated to " + status.name() + " and user notified.");
            redirectAttributes.addFlashAttribute("activeTab", "support");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "support");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/complaints/{id}/delete")
    public String deleteComplaint(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable("id") Long complaintId,
                                  RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.deleteComplaint(complaintId, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint #" + complaintId + " deleted successfully.");
            redirectAttributes.addFlashAttribute("activeTab", "support");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "support");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/feedback/delete/{id}")
    public String deleteFeedback(@PathVariable("id") Long feedbackId,
                                 RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.softDeleteFeedback(feedbackId);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback removed successfully.");
            redirectAttributes.addFlashAttribute("activeTab", "support");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("activeTab", "support");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/reports/generate")
    public String generateReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @ModelAttribute ReportRequestDTO dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            GeneratedReportDTO activeReport = reportFeedbackService.generateReportData(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("activeReport", activeReport);
            redirectAttributes.addFlashAttribute("activeTab", "reports");
            redirectAttributes.addFlashAttribute("successMessage", "Report '" + activeReport.getReport().getTitle() + "' generated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/reports/view/{id}")
    public String viewReport(@PathVariable("id") Long reportId,
                             RedirectAttributes redirectAttributes) {
        try {
            GeneratedReportDTO activeReport = reportFeedbackService.getReportDataById(reportId);
            redirectAttributes.addFlashAttribute("activeReport", activeReport);
            redirectAttributes.addFlashAttribute("activeTab", "reports");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
