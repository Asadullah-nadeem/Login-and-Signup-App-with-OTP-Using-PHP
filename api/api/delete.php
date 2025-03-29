<?php
// api/delete.php

// Include database configuration and mailer
require_once '../config/database.php';
require_once '../includes/mailer.php';

// Set headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: DELETE');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Check if request method is DELETE
if ($_SERVER['REQUEST_METHOD'] !== 'DELETE') {
    http_response_code(405);
    echo json_encode(['error' => 'Method Not Allowed']);
    exit;
}

// Get input data
$input = json_decode(file_get_contents('php://input'), true);

// Validate input
if (!isset($input['id']) || !is_numeric($input['id'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid or missing user ID']);
    exit;
}

$userId = (int)$input['id'];

try {
    // Fetch user email before deleting
    $stmt = $pdo->prepare("SELECT email FROM users WHERE id = ?");
    $stmt->execute([$userId]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$user) {
        http_response_code(404);
        echo json_encode(['error' => 'User not found']);
        exit;
    }

    // Delete user
    $stmt = $pdo->prepare("DELETE FROM users WHERE id = ?");
    $stmt->execute([$userId]);

    // Send email notification
    $email = $user['email'];
    $subject = "Your CodeAxe Account Has Been Deleted";
    
    // Modern UI Email Template
    $message = '
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 10px; padding: 20px; background-color: #f9f9f9;">
        <div style="text-align: center; padding: 10px;">
            <img src="https://codeaxe.co.in/logo.png" alt="CodeAxe Logo" style="max-width: 120px;">
        </div>
        <div style="background: white; padding: 20px; border-radius: 10px;">
            <h2 style="color: #333; text-align: center;">Account Deleted Successfully</h2>
            <p style="color: #555; font-size: 16px; text-align: center;">
                Hello,<br>
                Your CodeAxe account has been successfully deleted. If this was a mistake or you need assistance, feel free to contact our support team.
            </p>
            <div style="text-align: center; margin: 20px 0;">
                <a href="https://codeaxe.co.in/support" style="display: inline-block; padding: 12px 24px; background-color: #ff4c4c; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                    Contact Support
                </a>
            </div>
        </div>
        <div style="text-align: center; padding: 10px; font-size: 14px; color: #777;">
            <p>If you have any questions, please visit our <a href="https://codeaxe.co.in" style="color: #007bff; text-decoration: none;">website</a>.</p>
            <p>&copy; 2025 CodeAxe. All rights reserved.</p>
        </div>
    </div>';

    sendEmail($email, $subject, $message);

    // Response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'User deleted successfully and email notification sent'
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'error' => 'Database error: ' . $e->getMessage()
    ]);
}
?>
