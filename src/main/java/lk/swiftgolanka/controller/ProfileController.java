package lk.swiftgolanka.controller;

import jakarta.validation.Valid;
import lk.swiftgolanka.dto.PasswordUpdateDTO;
import lk.swiftgolanka.dto.UserProfileUpdateDTO;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.security.CustomUserDetails;
import lk.swiftgolanka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.getUserById(userDetails.getId());
        UserProfileUpdateDTO profileDTO = new UserProfileUpdateDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhone(user.getPhone());

        if (user.getRole() == Role.DRIVER) {
            DriverProfile dp = userService.getDriverProfileByUserId(user.getId());
            profileDTO.setLicenceNumber(dp.getLicenceNumber());
            profileDTO.setVehicleNumber(dp.getVehicleNumber());
            profileDTO.setVehicleModel(dp.getVehicleModel());
            profileDTO.setVehicleColour(dp.getVehicleColour());
            model.addAttribute("driverProfile", dp);
        }

        model.addAttribute("user", user);
        model.addAttribute("profileDTO", profileDTO);
        model.addAttribute("passwordDTO", new PasswordUpdateDTO());
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profileDTO") UserProfileUpdateDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide valid details.");
            return "redirect:/profile";
        }
        try {
            userService.updateUserProfile(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("passwordDTO") PasswordUpdateDTO dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/profile";
        }
        try {
            userService.updatePassword(userDetails.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
