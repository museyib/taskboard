package llc.devlab.app.security.service;

import llc.devlab.app.security.entity.LoginFail;
import llc.devlab.app.exception.UserLockException;
import llc.devlab.app.security.repository.LoginFailRepository;
import llc.devlab.app.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LoginFailService {

    public static final int MAX_LOGIN_FAILS = 5;
    public static final int LOCK_MINUTES = 1;

    private final LoginFailRepository loginFailRepository;
    private final UserRepository userRepository;

    @Transactional
    public int saveLoginFail(String email) {
        LoginFail loginFail = loginFailRepository.findByUsername(email).orElseGet(() -> {
            LoginFail loginFailNew = new LoginFail();
            loginFailNew.setUsername(email);
            loginFailNew.setTimestamp(System.currentTimeMillis());
            loginFailNew.setAttempts(0);
            return loginFailNew;
        });

        loginFail.setAttempts(loginFail.getAttempts() + 1);
        if (loginFail.getAttempts() >= MAX_LOGIN_FAILS) {
            loginFail.setLocked(true);
            loginFail.setLockedUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
            userRepository.updateByEmail(email, "LOCKED");
        }
        loginFailRepository.save(loginFail);
        return loginFail.getAttempts();
    }

    @Transactional
    public void unlockUser(String username) {
        loginFailRepository.deleteByUsername(username);
        userRepository.updateByEmail(username, "ACTIVE");
    }

    public void checkLockStatus(String username) {
        LoginFail loginFail = loginFailRepository.findByUsername(username).orElse(null);
        if (loginFail != null && loginFail.isLocked() && loginFail.getLockedUntil().isAfter(Instant.now())) {
            throw new UserLockException("User is locked until " + loginFail.getLockedUntil());
        }
    }
}
