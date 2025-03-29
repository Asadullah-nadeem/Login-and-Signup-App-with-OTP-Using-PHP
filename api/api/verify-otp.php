<?php
require_once '../config/constants.php';
require_once '../config/database.php';
require_once '../includes/otp.php';

header('Content-Type: application/json');

try {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (empty($data['email'])) {
        throw new Exception('Email is required');
    }
    
    if (empty($data['otp']) || !validateOTP($data['otp'])) {
        throw new Exception('Invalid OTP format (must be 4 digits)');
    }

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$data['email']]);
    $user = $stmt->fetch();

    if (!$user) {
        throw new Exception('Invalid request');
    }

    // OTP attempt tracking
    if ($user['otp_attempts'] >= 3 && strtotime($user['last_otp_attempt']) > time() - 300) {
        throw new Exception('Too many attempts. Try again later');
    }

    if ($user['otp'] !== $data['otp'] || strtotime($user['otp_expiry']) < time()) {
        // Increment attempt counter
        $stmt = $pdo->prepare("UPDATE users SET otp_attempts = otp_attempts + 1, last_otp_attempt = NOW() WHERE email = ?");
        $stmt->execute([$data['email']]);
        
        throw new Exception('Invalid or expired OTP');
    }

    // Reset attempts on success
    $stmt = $pdo->prepare("UPDATE users SET is_verified = 1, otp_attempts = 0, otp = NULL, otp_expiry = NULL WHERE email = ?");
    $stmt->execute([$data['email']]);

    echo json_encode(['status' => 'success', 'message' => 'Account verified']);
} catch (Exception $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>