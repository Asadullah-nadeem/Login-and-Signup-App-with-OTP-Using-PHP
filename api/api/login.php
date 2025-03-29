<?php
require_once '../config/constants.php';
require_once '../config/database.php';
require_once '../includes/mailer.php';
require_once '../includes/otp.php';

header('Content-Type: application/json');

try {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (empty($data['email']) || empty($data['password'])) {
        throw new Exception('Email and password are required');
    }

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$data['email']]);
    $user = $stmt->fetch();

    if (!$user) {
        throw new Exception('Invalid credentials');
    }
    // In your password change API
    $newPasswordHashed = password_hash($newPassword, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare("UPDATE users SET password = ? WHERE email = ?");
    $stmt->execute([$newPasswordHashed, $email]);
    // Account lock check
    if ($user['account_locked_until'] && strtotime($user['account_locked_until']) > time()) {
        throw new Exception('Account locked. Try again later');
    }

    if (!password_verify($data['password'], $user['password'])) {
    
        error_log("Password verification failed for: " . $data['email']);
        error_log("Submitted password: " . $data['password']);
        error_log("Stored hash: " . $user['password']);
        // Increment login attempts
        $newAttempts = $user['login_attempts'] + 1;
        $lockUntil = ($newAttempts >= MAX_LOGIN_ATTEMPTS) ? date('Y-m-d H:i:s', time() + ACCOUNT_LOCK_TIME) : null;
        
        $stmt = $pdo->prepare("UPDATE users SET login_attempts = ?, account_locked_until = ? WHERE email = ?");
        $stmt->execute([$newAttempts, $lockUntil, $data['email']]);
        
        throw new Exception('Invalid credentials');
    }

    if (!$user['is_verified']) {
        throw new Exception('Account not verified');
    }

    // Generate 4-digit login OTP
    $otp = generateOTP();
    $otpExpiry = date('Y-m-d H:i:s', time() + OTP_EXPIRY);
    
    $stmt = $pdo->prepare("UPDATE users SET otp = ?, otp_expiry = ?, otp_attempts = 0 WHERE email = ?");
    $stmt->execute([$otp, $otpExpiry, $data['email']]);

    // Send OTP
    $subject = "Your Login Code";

    $message = '
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login Verification</title>
    </head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; text-align: center;">
    
        <div style="max-width: 400px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); margin: auto;">
            <h2 style="color: #333;">Login Verification</h2>
            <p style="font-size: 16px; color: #555;">
                Use the following verification code to log in to your account. This code is valid for <strong>5 minutes</strong>.
            </p>
            <div style="font-size: 24px; font-weight: bold; color: #fff; background: #28a745; padding: 10px; border-radius: 5px; display: inline-block; margin: 10px 0;">
                '.$otp.'
            </div>
            <p style="font-size: 14px; color: #777;">
                If you did not request this code, please ignore this email.
            </p>
            <hr style="border: none; border-top: 1px solid #eee;">
            <p style="font-size: 12px; color: #999;">
                &copy; '.date("Y").' CodeAxe. All rights reserved.
            </p>
        </div>
    
    </body>
    </html>
    ';
    
    sendEmail($data['email'], $subject, $message);

    echo json_encode(['status' => 'success', 'message' => '4-digit OTP sent']);
} catch (Exception $e) {
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>