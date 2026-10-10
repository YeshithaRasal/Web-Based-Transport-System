package lk.swiftgolanka.repository;

import lk.swiftgolanka.entity.User;
import lk.swiftgolanka.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsDeletedFalse();

    List<User> findByRoleAndIsDeletedFalse(Role role);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsers(@Param("query") String query);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.role = :role AND " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchUsersByRole(@Param("query") String query, @Param("role") Role role);
}
