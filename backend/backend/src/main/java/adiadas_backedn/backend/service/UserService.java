package adiadas_backedn.backend.service;

import adiadas_backedn.backend.model.User;
import adiadas_backedn.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Lấy user bằng email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Lấy user bằng ID
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    // Lấy tất cả users (cho admin)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Cập nhật thông tin user
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // Xóa user
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Tăng số lần đăng nhập thất bại
    public void incrementLoginAttempts(String userId) {
        userRepository.incrementLoginAttempts(userId);
    }

    // Reset số lần đăng nhập thất bại và cập nhật last login
    public void updateLoginSuccess(String userId) {
        userRepository.updateLoginSuccess(userId, LocalDateTime.now());
    }

    // Khóa tài khoản
    public void lockAccount(String userId, LocalDateTime lockUntil) {
        userRepository.lockAccount(userId, lockUntil);
    }

    // Mở khóa tài khoản
    public void unlockAccount(String userId) {
        userRepository.unlockAccount(userId);
    }

    // Kiểm tra user có phải admin không
    public boolean isAdmin(String userId) {
        return userRepository.isAdmin(userId);
    }

    // Lấy danh sách admin users
    public List<User> getAdminUsers() {
        return userRepository.findAdminUsers();
    }

    // Tìm user bằng tên
    public List<User> findByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    // Lấy user bằng verification token
    public Optional<User> findByEmailVerificationToken(String token) {
        return userRepository.findByEmailVerificationToken(token);
    }

    // Lấy user bằng password reset token
    public Optional<User> findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    // Cập nhật cài đặt 2FA - ĐÃ SỬA
    @Transactional
    public boolean update2faSettings(String userId, boolean enabled, String secret) {
        try {
            // Đảm bảo secret không null khi enabled là true
            if (enabled && (secret == null || secret.trim().isEmpty())) {
                System.out.println("=== UPDATE 2FA ERROR: Secret is required when enabling 2FA ===");
                return false;
            }

            // Nếu disable, secret có thể là null
            String finalSecret = enabled ? secret : null;

            int updatedRows = userRepository.update2faSettings(userId, enabled, finalSecret);
            boolean success = updatedRows > 0;

            System.out.println("=== UPDATE 2FA SETTINGS: " + (success ? "SUCCESS" : "FAILED") + " ===");
            System.out.println("User ID: " + userId + ", Enabled: " + enabled);

            return success;
        } catch (Exception e) {
            System.out.println("=== UPDATE 2FA SETTINGS ERROR: " + e.getMessage() + " ===");
            return false;
        }
    }

    // Lấy users có 2FA enabled
    public List<User> getUsersWith2faEnabled() {
        return userRepository.findUsersWith2faEnabled();
    }

    // Cleanup tokens hết hạn
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        userRepository.cleanupExpiredVerificationTokens(now);
        userRepository.cleanupExpiredPasswordResetTokens(now);
    }

    // Lấy users có verification token hết hạn
    public List<User> getExpiredEmailVerifications() {
        return userRepository.findExpiredEmailVerifications(LocalDateTime.now());
    }

    // Lấy users có password reset token hết hạn
    public List<User> getExpiredPasswordResetTokens() {
        return userRepository.findExpiredPasswordResetTokens(LocalDateTime.now());
    }

    // Lấy users bị khóa tài khoản đã hết hạn
    public List<User> getExpiredAccountLocks() {
        return userRepository.findExpiredAccountLocks(LocalDateTime.now());
    }

    // Thống kê users
    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getVerifiedUsersCount() {
        return userRepository.countByEmailVerified(true);
    }

    public long getUnverifiedUsersCount() {
        return userRepository.countByEmailVerified(false);
    }
}