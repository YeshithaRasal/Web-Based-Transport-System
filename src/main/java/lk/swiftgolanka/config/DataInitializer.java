package lk.swiftgolanka.config;

import lk.swiftgolanka.entity.*;
import lk.swiftgolanka.enums.*;
import lk.swiftgolanka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverProfileRepository driverProfileRepository;

    @Autowired
    private RideBookingRepository bookingRepository;

    @Autowired
    private DriverAssignmentRepository assignmentRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DriverCommissionRepository commissionRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsersAndSystemData();
        }
    }

    private void seedUsersAndSystemData() {
        String commonPassword = passwordEncoder.encode("Password123");

        // 1. Admin
        User admin = userRepository.save(User.builder()
                .fullName("System Administrator")
                .email("admin@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 77 111 2222")
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        // 2. Operations Manager
        User ops = userRepository.save(User.builder()
                .fullName("Operations Lead")
                .email("ops@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 77 222 3333")
                .role(Role.OPERATIONS_MANAGER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        // 3. Customer Support
        User support = userRepository.save(User.builder()
                .fullName("Support Agent")
                .email("support@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 77 333 4444")
                .role(Role.CUSTOMER_SUPPORT)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        // 4. Finance Officer
        User finance = userRepository.save(User.builder()
                .fullName("Finance Officer")
                .email("finance@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 77 444 5555")
                .role(Role.FINANCE_OFFICER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        // 5. Passengers
        User passenger1 = userRepository.save(User.builder()
                .fullName("Kamal Perera")
                .email("passenger@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 71 555 6666")
                .role(Role.PASSENGER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        User passenger2 = userRepository.save(User.builder()
                .fullName("Nimali Fernando")
                .email("nimali@gmail.com")
                .password(commonPassword)
                .phone("+94 71 777 8888")
                .role(Role.PASSENGER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        // 6. Drivers
        User driverUser1 = userRepository.save(User.builder()
                .fullName("Sunil Shantha")
                .email("driver@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 76 888 9999")
                .role(Role.DRIVER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        DriverProfile driverProfile1 = driverProfileRepository.save(DriverProfile.builder()
                .user(driverUser1)
                .licenceNumber("DL-98765432")
                .vehicleNumber("CAB-1234")
                .vehicleType(VehicleType.CAR)
                .vehicleModel("Toyota Prius")
                .vehicleColour("Silver")
                .verificationStatus(VerificationStatus.VERIFIED)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .currentLat(6.9271)
                .currentLng(79.8612)
                .ratingAvg(4.8)
                .build());

        User driverUser2 = userRepository.save(User.builder()
                .fullName("Bandara Wickramasinghe")
                .email("driver2@swiftgolanka.lk")
                .password(commonPassword)
                .phone("+94 76 999 0000")
                .role(Role.DRIVER)
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build());

        DriverProfile driverProfile2 = driverProfileRepository.save(DriverProfile.builder()
                .user(driverUser2)
                .licenceNumber("DL-12345678")
                .vehicleNumber("TUK-5678")
                .vehicleType(VehicleType.TUKTUK)
                .vehicleModel("Bajaj RE 4T")
                .vehicleColour("Red")
                .verificationStatus(VerificationStatus.VERIFIED)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .currentLat(6.9000)
                .currentLng(79.8500)
                .ratingAvg(4.9)
                .build());

        // 7. Seed Sample Bookings, Trips, Payments, Ratings & Complaints
        RideBooking booking1 = bookingRepository.save(RideBooking.builder()
                .passenger(passenger1)
                .pickupLocation("Colombo Fort Railway Station")
                .pickupLat(6.9344)
                .pickupLng(79.8505)
                .destinationLocation("Galle Face Green, Colombo 03")
                .destinationLat(6.9271)
                .destinationLng(79.8447)
                .vehicleType(VehicleType.CAR)
                .estimatedDistanceKm(3.5)
                .estimatedDurationMin(12)
                .estimatedFare(500.0)
                .bookingStatus(BookingStatus.COMPLETED)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build());

        DriverAssignment assignment1 = assignmentRepository.save(DriverAssignment.builder()
                .booking(booking1)
                .driver(driverProfile1)
                .assignmentStatus(AssignmentStatus.ACCEPTED)
                .assignedAt(LocalDateTime.now().minusDays(2))
                .respondedAt(LocalDateTime.now().minusDays(2).plusMinutes(1))
                .build());

        Trip trip1 = tripRepository.save(Trip.builder()
                .booking(booking1)
                .passenger(passenger1)
                .driver(driverProfile1)
                .pickupLocation(booking1.getPickupLocation())
                .destinationLocation(booking1.getDestinationLocation())
                .startTime(LocalDateTime.now().minusDays(2).plusMinutes(2))
                .endTime(LocalDateTime.now().minusDays(2).plusMinutes(15))
                .distanceKm(3.5)
                .durationMin(13)
                .finalFare(500.0)
                .tripStatus(TripStatus.COMPLETED)
                .build());

        Payment payment1 = paymentRepository.save(Payment.builder()
                .trip(trip1)
                .passenger(passenger1)
                .amount(500.0)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PAID)
                .transactionReference("TXN-SWIFT-982104")
                .paidAt(LocalDateTime.now().minusDays(2).plusMinutes(16))
                .build());

        commissionRepository.save(DriverCommission.builder()
                .trip(trip1)
                .driver(driverProfile1)
                .tripFare(500.0)
                .commissionRatePct(15.0)
                .driverEarnings(425.0)
                .platformFee(75.0)
                .createdAt(LocalDateTime.now().minusDays(2).plusMinutes(16))
                .build());

        feedbackRepository.save(Feedback.builder()
                .trip(trip1)
                .passenger(passenger1)
                .driver(driverProfile1)
                .rating(5)
                .comment("Excellent driver! Prompt pickup and clean vehicle.")
                .createdAt(LocalDateTime.now().minusDays(2).plusMinutes(20))
                .isDeleted(false)
                .build());

        // Complaint sample
        complaintRepository.save(Complaint.builder()
                .user(passenger2)
                .trip(null)
                .subject("Payment delay inquiry")
                .category("Billing")
                .description("I was charged twice on my card for booking #102 yesterday.")
                .complaintStatus(ComplaintStatus.OPEN)
                .createdAt(LocalDateTime.now().minusHours(5))
                .updatedAt(LocalDateTime.now().minusHours(5))
                .build());

        // Report sample
        reportRepository.save(Report.builder()
                .reportType(ReportType.RIDE_ACTIVITY)
                .title("Monthly Operational Summary - August 2026")
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .generatedBy(admin)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build());

        // Notification sample
        notificationRepository.save(Notification.builder()
                .user(passenger1)
                .title("Welcome to Swift Go Lanka!")
                .message("Your passenger account is active. Book your first safe ride with us today.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
