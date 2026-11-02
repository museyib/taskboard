package llc.devlab.app.security.repository;

import llc.devlab.app.security.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE AppUser u SET u.status = :status WHERE u.email = :email")
    void updateByEmail(@Param("email") String email, @Param("status") String status);
}
