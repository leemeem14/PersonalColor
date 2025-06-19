package kr.ac.kopo.lyh.personalcolor.controller;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.lyh.personalcolor.controller.dto.LoginRequest;
import kr.ac.kopo.lyh.personalcolor.controller.dto.LoginResponse;
import kr.ac.kopo.lyh.personalcolor.controller.dto.SignupForm;
import kr.ac.kopo.lyh.personalcolor.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm signupForm,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        try {
            userService.createUser(signupForm);
            model.addAttribute("successMessage", "회원가입이 완료되었습니다.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        try {
            // 사용자 인증
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            // Spring Security 인증 토큰 생성
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // 세션에 Spring Security 컨텍스트 저장
            HttpSession session = httpRequest.getSession();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            // 추가 사용자 정보 세션에 저장
            session.setAttribute("user", user);
            session.setAttribute("isLoggedIn", true);

            log.info("사용자 로그인 성공: {}", user.getEmail());

            return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .redirectUrl("/index")
                    .build());

        } catch (Exception e) {
            log.error("로그인 실패", e);
            return ResponseEntity.ok(LoginResponse.builder()
                    .success(false)
                    .message("이메일 또는 비밀번호가 올바르지 않습니다.")
                    .build());
        }
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        log.info("사용자 로그아웃");
        return "redirect:/home";
    }
}
