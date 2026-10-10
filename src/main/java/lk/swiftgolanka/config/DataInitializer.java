package lk.swiftgolanka.config;

import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.AccountStatus;
import lk.swiftgolanka.enums.Role;
import lk.swiftgolanka.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdminUser();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@swiftgolanka.lk";
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .fullName("System Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .phone("+94 77 111 2222")
                    .role(Role.ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .isDeleted(false)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default System Administrator account ({}) with password 'admin123'", adminEmail);
        } else {
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setAccountStatus(AccountStatus.ACTIVE);
            admin.setDeleted(false);
            userRepository.save(admin);
            log.info("Synchronized System Administrator account ({}) password to 'admin123'", adminEmail);
        }
    }
}
