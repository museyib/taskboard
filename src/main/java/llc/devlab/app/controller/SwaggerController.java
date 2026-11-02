package llc.devlab.app.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class SwaggerController {

    @GetMapping("/swagger")
    public String swagger() {
        return "swagger-ui.html";
    }
}
