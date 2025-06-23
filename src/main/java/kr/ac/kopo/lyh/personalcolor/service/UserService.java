package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.controller.dto.SignupForm;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.exception.UserNotFoundException;
import kr.ac.kopo.lyh.personalcolor.exception.DuplicateEmailException;
import kr.ac.kopo.lyh.personalcolor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 서비스 - Spring Boot 3.4 / Spring Security 6.1 최적화
 * UserDetailsService 구현으로 Spring Security 통합
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Security UserDetailsService 구현
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 인증 시도: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 회원 가입
     */
    @Transactional
    public User createUser(SignupForm signupForm) {
        log.info("새 사용자 가입 시도: {}", signupForm.getEmail());

        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupForm.getEmail())) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + signupForm.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupForm.getPassword());

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(signupForm.getEmail())
                .password(encodedPassword)
                .name(signupForm.getName())
                .gender(signupForm.getGender())
                .role(User.Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 가입 완료: {}", savedUser.getEmail());

        return savedUser;
    }

    /**
     * 사용자 인증 (로그인)
     */
    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        log.debug("사용자 인증: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다: " + email));

        if (!user.isEnabled()) {
            throw new UserNotFoundException("비활성화된 계정입니다: " + email);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        log.info("사용자 인증 성공: {}", email);
        return user;
    }

    /**
     * 이메일로 사용자 조회
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 조회 (필수)
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * ID로 사용자 조회
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }

    /**
     * 모든 활성 사용자 조회
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByEnabledTrue();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        log.info("사용자 비밀번호 변경 완료: {}", user.getEmail());
    }

    /**
     * 사용자 정보 업데이트
     */
    @Transactional
    public User updateUser(Long userId, String name, User.Gender gender) {
        User user = getUserById(userId);

        user.setName(name);
        user.setGender(gender);

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 업데이트 완료: {}", updatedUser.getEmail());

        return updatedUser;
    }

    /**
     * 사용자 계정 비활성화
     */
    @Transactional
    public void deactivateUser(Long userId) {
        userRepository.deactivateUser(userId);
        log.info("사용자 계정 비활성화: {}", userId);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}