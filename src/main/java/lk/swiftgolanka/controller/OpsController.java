package lk.swiftgolanka.controller;

import lk.swiftgolanka.dto.ReportRequestDTO;
import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.ReportType;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ops")
public class OpsController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private DriverAssignmentService assignmentService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @GetMapping("/dashboard")
    public String opsDashboard(Model model) {
        List<RideBooking> recentBookings = bookingService.getAllRecentBookings();
        List<DriverProfile> drivers = userService.getAllDrivers();
        List<Trip> trips = tripService.getAllTrips();

        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("drivers", drivers);
        model.addAttribute("trips", trips);
        model.addAttribute("reportRequestDTO", new ReportRequestDTO());
        model.addAttribute("reportTypes", ReportType.values());

        return "ops/dashboard";
    }

    @PostMapping("/assignments/manual-assign")
    public String manualAssignDriver(@RequestParam("bookingId") Long bookingId,
                                     @RequestParam("driverId") Long driverProfileId,
                                     RedirectAttributes redirectAttributes) {
        try {
            assignmentService.manualAssignDriver(bookingId, driverProfileId);
            redirectAttributes.addFlashAttribute("successMessage", "Driver manually assigned to booking #" + bookingId);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/ops/dashboard";
    }

    @PostMapping("/reports/generate")
    public String generateOperationalReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @ModelAttribute ReportRequestDTO dto,
                                            RedirectAttributes redirectAttributes) {
        try {
            dto.setReportType(ReportType.OPERATIONAL_PERFORMANCE);
            reportFeedbackService.generateReport(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Operational Report generated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/ops/dashboard";
    }
}
