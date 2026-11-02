package llc.devlab.app.security.repository;

import llc.devlab.app.security.entity.LoginFail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginFailRepository extends JpaRepository<LoginFail, Long> {

    Optional<LoginFail> findByUsername(String username);

    void deleteByUsername(String username);
}
