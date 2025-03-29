<?php
require_once '../config/constants.php';
require_once '../config/database.php';
require_once '../includes/mailer.php';

header('Content-Type: application/json');

try {
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (empty($data['email'])) {
        throw new Exception('Email is required');
    }

    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$data['email']]);
    $user = $stmt->fetch();

    if ($user) {
        $token = bin2hex(random_bytes(32));
        $expiry = date('Y-m-d H:i:s', time() + RESET_TOKEN_EXPIRY);
        
        $stmt = $pdo->prepare("UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?");
        $stmt->execute([$token, $expiry, $data['email']]);

     $resetLink = "https://codeaxe.co.in/api/reset-password.php?token=$token";
$subject = "Password Reset Request";

$message = '
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Password Reset</title>
    </head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; text-align: center;">

        <div style="max-width: 400px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); margin: auto;">
            <h2 style="color: #333;">Password Reset Request</h2>
            <p style="font-size: 16px; color: #555;">
                You have requested to reset your password. Click the button below to proceed.
            </p>
            <a href="'.$resetLink.'" style="display: inline-block; padding: 12px 20px; font-size: 16px; font-weight: bold; color: white; background: #007bff; text-decoration: none; border-radius: 5px; margin: 10px 0;">
                Reset Password
            </a>
            <p style="font-size: 14px; color: #777;">
                If you did not request this, please ignore this email. The link will expire in 15 minutes.
            </p>
            <p>'.$resetLink.'</p>
            <hr style="border: none; border-top: 1px solid #eee;">
            <p style="font-size: 12px; color: #999;">
                &copy; '.date("Y").' CodeAxe. All rights reserved.
            </p>
        </div>

    </body>
    </html>
    ';
        sendEmail($data['email'], $subject, $message);
    }

        echo json_encode(['status' => 'success', 'message' => 'If email exists, reset link sent']);
    } catch (Exception $e) {
        echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
    }
 ?>