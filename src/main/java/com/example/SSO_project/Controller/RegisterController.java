package com.example.SSO_project.Controller;

import com.example.SSO_project.Exception.UserAlreadyExists;
import com.example.SSO_project.Service.UserRegisterationService;
import com.example.SSO_project.domain.RegisterRequest;
import com.example.SSO_project.domain.UserRegister;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserRegisterationService userRegisterationService;

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest req,
                           BindingResult binding,
                           Model model){
        if (binding.hasErrors()) {
            return "register";
        }
        try{
            userRegisterationService.registerUser(req);
            return "redirect:/login?registered";
        }catch (UserAlreadyExists e){
            model.addAttribute("error",e.getMessage());
            return "register";
        }
    }
}
