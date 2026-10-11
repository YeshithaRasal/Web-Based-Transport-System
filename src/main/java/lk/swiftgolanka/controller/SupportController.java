package lk.swiftgolanka.controller;

import lk.swiftgolanka.entity.Complaint;
import lk.swiftgolanka.entity.Feedback;
import lk.swiftgolanka.enums.ComplaintStatus;
import lk.swiftgolanka.service.ReportFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @GetMapping("/dashboard")
    public String supportDashboard(Model model) {
        List<Complaint> complaints = reportFeedbackService.getAllComplaints();
        List<Feedback> feedbackList = reportFeedbackService.getAllFeedback();

        model.addAttribute("complaints", complaints);
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("statuses", ComplaintStatus.values());

        return "support/dashboard";
    }

    @PostMapping("/complaints/update-status")
    public String updateComplaintStatus(@RequestParam("complaintId") Long complaintId,
                                        @RequestParam("status") ComplaintStatus status,
                                        @RequestParam(value = "resolutionNotes", required = false) String resolutionNotes,
                                        RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.updateComplaintStatus(complaintId, status, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Complaint #" + complaintId + " updated to " + status.name());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/support/dashboard";
    }

    @PostMapping("/feedback/delete/{id}")
    public String deleteFeedback(@PathVariable("id") Long feedbackId, RedirectAttributes redirectAttributes) {
        try {
            reportFeedbackService.softDeleteFeedback(feedbackId);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback soft-deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/support/dashboard";
    }
}
