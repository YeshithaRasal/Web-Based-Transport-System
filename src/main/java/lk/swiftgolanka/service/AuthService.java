package lk.swiftgolanka.service;

import lk.swiftgolanka.dto.DriverRegisterDTO;
import lk.swiftgolanka.dto.PassengerRegisterDTO;
import lk.swiftgolanka.dto.StaffRegisterDTO;
import lk.swiftgolanka.entity.DriverProfile;
import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AccountStatus;
import lk.swiftgolanka.enums.AvailabilityStatus;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.enums.VerificationStatus;
import lk.swiftgolanka.exception.InvalidOperationException;
import lk.swiftgolanka.repository.DriverProfileRepository;
import lk.swiftgolanka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerPassenger(PassengerRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new InvalidOperationException("Email address is already registered");
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(Role.PASSENGER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User registerDriver(DriverRegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new InvalidOperationException("Email address is already registered");
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(Role.DRIVER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);

        DriverProfile driverProfile = DriverProfile.builder()
                .user(savedUser)
                .licenceNumber(dto.getLicenceNumber())
                .vehicleNumber(dto.getVehicleNumber())
                .vehicleType(dto.getVehicleType())
                .vehicleModel(dto.getVehicleModel())
                .vehicleColour(dto.getVehicleColour())
                .verificationStatus(VerificationStatus.VERIFIED)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .ratingAvg(5.0)
                .build();

        driverProfileRepository.save(driverProfile);
        return savedUser;
    }

    @Transactional
    public User createStaffUser(StaffRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new InvalidOperationException("Email address is already registered");
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();

        return userRepository.save(user);
    }
}
