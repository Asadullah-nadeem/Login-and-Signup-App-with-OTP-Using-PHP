<?php
require_once '../config/constants.php';
require_once '../config/database.php';
require_once '../includes/mailer.php';
require_once '../includes/otp.php';

header('Content-Type: application/json');

try {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (empty($data['full_name']) || empty($data['email']) || empty($data['password'])) {
        throw new Exception('All fields are required');
    }

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$data['email']]);
    if ($stmt->rowCount() > 0) {
        throw new Exception('Email already exists');
    }

    $otp = generateOTP();
    $hashedPassword = password_hash($data['password'], PASSWORD_DEFAULT);
    $otpExpiry = date('Y-m-d H:i:s', time() + OTP_EXPIRY);

    $stmt = $pdo->prepare("INSERT INTO users (full_name, email, password, otp, otp_expiry) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$data['full_name'], $data['email'], $hashedPassword, $otp, $otpExpiry]);

    // Send OTP email
    $subject = "Verify Your Account";
    $message = '
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Account Verification</title>
    </head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; text-align: center;">
    
        <div style="max-width: 400px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); margin: auto;">
            <h2 style="color: #333;">Verify Your Account</h2>
            <p style="font-size: 16px; color: #555;">
                Use the following verification code to complete your signup process. This code is valid for <strong>5 minutes</strong>.
            </p>
            <div style="font-size: 24px; font-weight: bold; color: #fff; background: #007bff; padding: 10px; border-radius: 5px; display: inline-block; margin: 10px 0;">
                '.$otp.'
            </div>
            <p style="font-size: 14px; color: #777;">
                If you did not request this, please ignore this email.
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