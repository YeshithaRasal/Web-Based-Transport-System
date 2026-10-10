package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.PasswordUpdateDTO;
import lk.swiftgolanka.dto.UserProfileUpdateDTO;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AccountStatus;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.enums.VerificationStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.exception.ResourceNotFoundException;
import lk.swiftgolanka.repository.DriverProfileRepository;
import lk.swiftgolanka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    public List<User> getAllActiveUsers() {
        return userRepository.findByIsDeletedFalse();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndIsDeletedFalse(role);
    }

    public List<User> searchUsers(String query, Role role) {
        if (query == null || query.isBlank()) {
            return role == null ? getAllActiveUsers() : getUsersByRole(role);
        }
        if (role != null) {
            return userRepository.searchUsersByRole(query, role);
        }
        return userRepository.searchUsers(query);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public DriverProfile getDriverProfileByUserId(Long userId) {
        return driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));
    }

    public List<DriverProfile> getAllDrivers() {
        return driverProfileRepository.findAll();
    }

    @Transactional
    public void verifyDriver(Long driverProfileId, boolean approve) {
        DriverProfile driverProfile = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found with ID: " + driverProfileId));

        if (approve) {
            driverProfile.setVerificationStatus(VerificationStatus.VERIFIED);
            notificationService.sendNotification(driverProfile.getUser(), "Account Verified!", "Your driver profile has been approved by Admin. You can now switch your status to AVAILABLE to accept rides.");
        } else {
            driverProfile.setVerificationStatus(VerificationStatus.REJECTED);
            notificationService.sendNotification(driverProfile.getUser(), "Verification Rejected", "Your driver verification request was rejected. Please update your document details.");
        }
        driverProfileRepository.save(driverProfile);
    }

    @Transactional
    public void toggleAccountStatus(Long userId) {
        User user = getUserById(userId);
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            user.setAccountStatus(AccountStatus.SUSPENDED);
            notificationService.sendNotification(user, "Account Suspended", "Your account has been suspended by an Administrator.");
        } else {
            user.setAccountStatus(AccountStatus.ACTIVE);
            notificationService.sendNotification(user, "Account Activated", "Your account is now active.");
        }
        userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = getUserById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Transactional
    public User updateUserProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = getUserById(userId);
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());

        if (user.getRole() == Role.DRIVER) {
            DriverProfile dp = getDriverProfileByUserId(userId);
            if (dto.getLicenceNumber() != null) dp.setLicenceNumber(dto.getLicenceNumber());
            if (dto.getVehicleNumber() != null) dp.setVehicleNumber(dto.getVehicleNumber());
            if (dto.getVehicleModel() != null) dp.setVehicleModel(dto.getVehicleModel());
            if (dto.getVehicleColour() != null) dp.setVehicleColour(dto.getVehicleColour());
            driverProfileRepository.save(dp);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
