package llc.devlab.app.security.mapper;

import llc.devlab.app.security.dto.RegisterRequest;
import llc.devlab.app.security.dto.UserDto;
import llc.devlab.app.security.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AppUser toEntity(RegisterRequest request) {
        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setRole(request.role() != null && !request.role().isBlank() ? request.role() : "USER");
        user.setStatus("ACTIVE");
        return user;
    }

    public UserDto toDto(AppUser user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus()
        );
    }
}
