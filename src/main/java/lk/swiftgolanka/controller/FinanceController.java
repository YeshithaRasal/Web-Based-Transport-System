package lk.swiftgolanka.controller;

import lk.swiftgolanka.dto.ReportRequestDTO;
import lk.swiftgolanka.entity.Payment;
import lk.swiftgolanka.entity.Refund;
import lk.swiftgolanka.enums.ReportType;
import lk.swiftgolanka.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportFeedbackService reportFeedbackService;

    @GetMapping("/dashboard")
    public String financeDashboard(Model model) {
        List<Payment> payments = paymentService.getAllPayments();
        List<Refund> refunds = paymentService.getAllRefunds();
        Double totalRevenue = paymentService.getTotalRevenue();
        Double platformFees = paymentService.getTotalPlatformFees();

        model.addAttribute("payments", payments);
        model.addAttribute("refunds", refunds);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("platformFees", platformFees);
        model.addAttribute("reportRequestDTO", new ReportRequestDTO());
        model.addAttribute("reportTypes", ReportType.values());

        return "finance/dashboard";
    }

    @PostMapping("/refunds/process")
    public String processRefund(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("refundId") Long refundId,
                                @RequestParam("approve") boolean approve,
                                @RequestParam(value = "remarks", required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            paymentService.processRefund(refundId, approve, remarks, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Refund request " + (approve ? "APPROVED & PROCESSED" : "REJECTED") + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/finance/dashboard";
    }

    @PostMapping("/reports/generate")
    public String generateFinancialReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @ModelAttribute ReportRequestDTO dto,
                                          RedirectAttributes redirectAttributes) {
        try {
            dto.setReportType(ReportType.FINANCIAL);
            reportFeedbackService.generateReport(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Financial Report generated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/finance/dashboard";
    }
}
