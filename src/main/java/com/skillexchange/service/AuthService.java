package com.skillexchange.service;

import com.skillexchange.model.ApiResponse;
import com.skillexchange.model.OtpDetails;
import com.skillexchange.model.UserDetails;
import com.skillexchange.model.dto.LoginDTO;
import com.skillexchange.service.JwtService;
import com.skillexchange.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthRepository authRepo;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In-memory OTP store: email(lowercased) -> OTP details
    private final Map<String, OtpDetails> otpStore = new HashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long OTP_TTL_MILLIS = 5 * 60 * 1000L; // 5 minutes

    public AuthService(AuthRepository authRepo, EmailService emailService, JwtService jwtService) {
        this.authRepo = authRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    public ResponseEntity<?> signupToOtp(UserDetails signupRequest) {
        try {
            if (signupRequest == null || isBlank(signupRequest.getEmail())) {
                return ResponseEntity.ok(new ApiResponse<>(false, "Email is required", null));
            }
            String userEmail = signupRequest.getEmail();
            if (authRepo.existsByEmailIgnoreCase(userEmail)) {
                return ResponseEntity.ok(new ApiResponse<>(false, "User already exists", null));
            }
            return generateOtp(userEmail);
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    public ResponseEntity<?> generateOtp(String email) {
        if (isBlank(email)) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Email is required", null));
        }

        int otp = 100_000 + RANDOM.nextInt(900_000); // 6-digit OTP

        // Save OTP with normalized key
        String key = email.toLowerCase();
        otpStore.put(key, new OtpDetails(otp, System.currentTimeMillis()));

        // Send OTP via email
        emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);

        return ResponseEntity.ok(new ApiResponse<>(true, "Otp Sent Successfully", null));
    }

    public ResponseEntity<?> signup(UserDetails signupRequest) {
        String hash = passwordEncoder.encode(signupRequest.getPassword());

        UserDetails user = UserDetails.builder()
            .name(signupRequest.getName())
            .email(signupRequest.getEmail())
            .phone(signupRequest.getPhone())
            .skills(signupRequest.getSkills())
            .password(hash)
            .githubProfile(signupRequest.getGithubProfile())
            .linkedinProfile(signupRequest.getLinkedinProfile())
            .youtubeProfile(signupRequest.getYoutubeProfile())
            .instagramProfile(signupRequest.getInstagramProfile())
            .bio(signupRequest.getBio())
            .profilePictureUrl(signupRequest.getProfilePictureUrl())
            .build();

        authRepo.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Signup Successfully", null));
    }

    public ResponseEntity<?> verifyOtp(int enteredOtp, UserDetails signupRequest) {
        if (signupRequest == null || isBlank(signupRequest.getEmail())) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Email is required", null));
        }

        String emailKey = signupRequest.getEmail().toLowerCase();
        OtpDetails otpDetails = otpStore.get(emailKey);

        if (otpDetails == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Otp Is Null", null));
        }

        long now = System.currentTimeMillis();
        if (now - otpDetails.getTimestamp() > OTP_TTL_MILLIS) { // expired
            otpStore.remove(emailKey);
            return ResponseEntity.ok(new ApiResponse<>(false, "Otp Expire", null));
        }

        if (otpDetails.getOtp() != enteredOtp) {
            return ResponseEntity.ok(new ApiResponse<>(false, "Otp Is Invalid", null));
        }

        // OTP verified – remove only this email's entry
        otpStore.remove(emailKey);

        // Basic required fields check
        if (!checkForEmptyField(signupRequest)) {
            return ResponseEntity.ok(new ApiResponse<>(false, "please enter all details", null));
        }

        ResponseEntity<?> signupResponse = signup(signupRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "signup Successfully", signupResponse));
    }

    public Boolean checkForEmptyField(UserDetails signUpRequest) {
        if (signUpRequest == null) return false;
        return !isBlank(signUpRequest.getEmail())
               && !isBlank(signUpRequest.getPhone())
               && !isBlank(signUpRequest.getPassword())
               && !isBlank(signUpRequest.getName())
               && signUpRequest.getSkills() != null
               && !signUpRequest.getSkills().isEmpty();
    }

    public ResponseEntity<?> login(LoginDTO loginDto) {
        try {
            if (loginDto == null || isBlank(loginDto.getEmail()) || isBlank(loginDto.getPassword())) {
                return ResponseEntity.ok(new ApiResponse<>(false, "Email and password are required", null));
            }

            UserDetails user = authRepo.findByEmailIgnoreCase(loginDto.getEmail())
                                       .orElseThrow(() -> new RuntimeException("user not found"));

            if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid Credentials");
            }

            String jwt = jwtService.generateToken(loginDto.getEmail());
            return ResponseEntity.ok(new ApiResponse<>(true, "Login Successfully", jwt));

        } catch (RuntimeException e) {
            return ResponseEntity.ok(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ApiResponse<>(false, "Something went wrong", e.getMessage()));
        }
    }

    public Optional<UserDetails> findByEmailIgnoreCase(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return authRepo.findByEmailIgnoreCase(email);
    }

    public UserDetails updateUserProfile(UserDetails user) {
        if (user == null || user.getEmail() == null) return null;
        return authRepo.save(user);
    }

    public Optional<UserDetails> findByUserId(UUID userId) {
        if (userId == null) return Optional.empty();
        return authRepo.findById(userId);
    }

    /* ---------- helpers ---------- */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
