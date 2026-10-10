package lk.swiftgolanka.controller;

import jakarta.validation.Valid;
import lk.swiftgolanka.dto.DriverRegisterDTO;
import lk.swiftgolanka.dto.PassengerRegisterDTO;
import lk.swiftgolanka.enums.VehicleType;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            String role = userDetails.getUser().getRole().name();
            switch (role) {
                case "PASSENGER": return "redirect:/passenger/dashboard";
                case "DRIVER": return "redirect:/driver/dashboard";
                case "ADMIN": return "redirect:/admin/dashboard";
                case "OPERATIONS_MANAGER": return "redirect:/ops/dashboard";
                case "CUSTOMER_SUPPORT": return "redirect:/support/dashboard";
                case "FINANCE_OFFICER": return "redirect:/finance/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully logged out.");
        }
        return "login";
    }

    @GetMapping("/register/passenger")
    public String registerPassengerForm(Model model) {
        model.addAttribute("passengerDTO", new PassengerRegisterDTO());
        return "register-passenger";
    }

    @PostMapping("/register/passenger")
    public String handlePassengerRegistration(@Valid @ModelAttribute("passengerDTO") PassengerRegisterDTO dto,
                                              BindingResult bindingResult,
                                              Model model,
                                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register-passenger";
        }
        try {
            authService.registerPassenger(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in with your email.");
            return "redirect:/login";
        } catch (InvalidOperationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register-passenger";
        }
    }

    @GetMapping("/register/driver")
    public String registerDriverForm(Model model) {
        model.addAttribute("driverDTO", new DriverRegisterDTO());
        model.addAttribute("vehicleTypes", VehicleType.values());
        return "register-driver";
    }

    @PostMapping("/register/driver")
    public String handleDriverRegistration(@Valid @ModelAttribute("driverDTO") DriverRegisterDTO dto,
                                           BindingResult bindingResult,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vehicleTypes", VehicleType.values());
            return "register-driver";
        }
        try {
            authService.registerDriver(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Driver registration successful! Your profile is verified and active. You can now log in.");
            return "redirect:/login";
        } catch (InvalidOperationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("vehicleTypes", VehicleType.values());
            return "register-driver";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("successMessage", "If an account exists with " + email + ", a password reset link has been dispatched.");
        return "redirect:/login";
    }
}
