<?php
// reset-password.php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Database and configuration setup
try {
    require_once '../config/constants.php';
    require_once '../config/database.php';
    require_once '../includes/mailer.php';
} catch (Exception $e) {
    error_log("Config loading error: " . $e->getMessage());
    http_response_code(500);
    die("Server configuration error. Please contact administrator.");
}

// Set proper headers
header('Content-Type: text/html; charset=utf-8');

// Get token from URL
$token = isset($_GET['token']) ? trim($_GET['token']) : '';
$error = '';
$success = false;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $data = $_POST;
        
        // Validate inputs
        if (empty($token) || empty($data['password']) || empty($data['confirm_password'])) {
            throw new Exception('All fields are required');
        }

        if ($data['password'] !== $data['confirm_password']) {
            throw new Exception('Passwords do not match');
        }

        if (strlen($data['password']) < 8) {
            throw new Exception('Password must be at least 8 characters');
        }

        // Begin transaction
        $pdo->beginTransaction();

        // Debug: Show token and current time
        error_log("Token being validated: " . $token);
        error_log("Current time: " . date('Y-m-d H:i:s'));

        // Check token validity and get user
        $stmt = $pdo->prepare("SELECT id, email, reset_token_expiry FROM users WHERE reset_token = ?");
        if (!$stmt->execute([$token])) {
            throw new Exception('Database query failed');
        }
        
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$user) {
            throw new Exception('Invalid token');
        }

        // Debug: Show token expiry time
        error_log("Token expiry time: " . $user['reset_token_expiry']);

        // Check if token is expired
        $currentTime = time();
        $expiryTime = strtotime($user['reset_token_expiry']);
        
        if ($expiryTime < $currentTime) {
            $minutesLeft = round(($expiryTime - $currentTime) / 60, 2);
            throw new Exception('Token expired. Please request a new password reset link.');
        }

        // Update password and clear token
        $hashedPassword = password_hash($data['password'], PASSWORD_BCRYPT);
        $updateStmt = $pdo->prepare("UPDATE users SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE id = ?");
        
        if (!$updateStmt->execute([$hashedPassword, $user['id']])) {
            throw new Exception('Password update failed');
        }

        // Send confirmation email
        $subject = "Password Reset Successfully";
        $message = generateEmailTemplate($user['email']);
        
        if (!sendEmail($user['email'], $subject, $message)) {
            error_log("Failed to send email to " . $user['email']);
        }

        // Commit transaction if everything succeeded
        $pdo->commit();
        $success = true;

    } catch (Exception $e) {
        // Rollback on error
        if (isset($pdo) && $pdo->inTransaction()) {
            $pdo->rollBack();
        }
        
        $error = $e->getMessage();
        error_log("Password reset error: " . $e->getMessage());
    }
}

function generateEmailTemplate($email) {
    return '
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Password Reset Confirmation</title>
    </head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; text-align: center;">
        <div style="max-width: 400px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); margin: auto;">
            <h2 style="color: #333;">Password Reset Successful</h2>
            <p style="font-size: 16px; color: #555;">
                Your password has been successfully reset on ' . date('Y-m-d H:i:s') . '.
            </p>
            <p style="font-size: 14px; color: #777;">
                If you did not perform this action, please contact our support team immediately.
            </p>
            <div style="margin: 20px 0;">
                <a href="myapp://com.example.myapplication/login" style="display: inline-block; padding: 12px 20px; font-size: 16px; font-weight: bold; color: white; background: #007bff; text-decoration: none; border-radius: 5px; margin: 5px;">
                    Open in App
                </a>
            </div>
            <hr style="border: none; border-top: 1px solid #eee;">
            <p style="font-size: 12px; color: #999;">
                © ' . date("Y") . ' CodeAxe. All rights reserved.
            </p>
        </div>
    </body>
    </html>';
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <link rel="stylesheet" href="../assets/css/style.css" />
    <style>
        .error-message { 
            color: red; 
            text-align: center; 
            margin: 10px 0; 
            padding: 10px;
            background: #ffeeee;
            border-radius: 5px;
        }
        .token-info {
            color: #666;
            font-size: 14px;
            text-align: center;
            margin: 10px 0;
        }
        .success-message { display: none; flex-direction: column; align-items: center; }
        .tick-mark { width: 50px; height: 50px; background: #28a745; border-radius: 50%; margin: 20px; }
        .tick-mark::before { content: "✓"; color: white; font-size: 30px; display: block; text-align: center; line-height: 50px; }
        .password-strength {
            height: 5px;
            margin-top: 5px;
            background: #eee;
            border-radius: 3px;
            overflow: hidden;
        }
        .strength-0 { width: 20%; background: #ff4d4d; }
        .strength-1 { width: 40%; background: #ff9933; }
        .strength-2 { width: 60%; background: #ffcc00; }
        .strength-3 { width: 80%; background: #99cc33; }
        .strength-4 { width: 100%; background: #33cc33; }
    </style>
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="card-header">
                <h2>Reset Password</h2>
                <p>Enter your new password below</p>
                <?php if ($token): ?>
                    <div class="token-info">
                        Token valid for: 15 minutes
                    </div>
                <?php endif; ?>
            </div>
            
            <?php if ($error): ?>
                <div class="error-message"><?php echo htmlspecialchars($error); ?></div>
            <?php endif; ?>

            <form id="resetForm" method="POST" <?php echo $success ? 'style="display: none;"' : ''; ?>>
                <input type="hidden" name="token" value="<?php echo htmlspecialchars($token); ?>">
                <div class="form-group">
                    <label for="new-password">New Password</label>
                    <input type="password" id="new-password" name="password" placeholder="Enter new password" required 
                           oninput="checkPasswordStrength(this.value)">
                    <span class="show-password" onclick="togglePassword('new-password')">
                        <i class="fas fa-eye"></i>
                    </span>
                    <div class="password-strength">
                        <div id="password-strength-bar"></div>
                    </div>
                    <small id="password-strength-text" style="display: block; font-size: 12px; color: #666;"></small>
                </div>
                <div class="form-group">
                    <label for="confirm-password">Confirm Password</label>
                    <input type="password" id="confirm-password" name="confirm_password" placeholder="Confirm new password" required>
                    <span class="show-password" onclick="togglePassword('confirm-password')">
                        <i class="fas fa-eye"></i>
                    </span>
                    <small id="password-match" style="display: none; color: red;">Passwords don't match</small>
                </div>
                <button type="submit" class="submit-btn" id="submit-btn">Reset Password</button>
            </form>

            <div class="success-message" id="successMessage" <?php echo $success ? 'style="display: flex;"' : ''; ?>>
                <div class="tick-mark"></div>
                <h3>Success!</h3>
                <p>Your password has been successfully changed. Check your email for confirmation.</p>
                <a href="myapp://com.example.myapplication/login" class="app-link">Open in App</a>
            </div>
        </div>
    </div>

    <script>
        function togglePassword(inputId) {
            const input = document.getElementById(inputId);
            const toggle = input.nextElementSibling.firstElementChild;
            const isPassword = input.type === "password";
            
            input.type = isPassword ? "text" : "password";
            toggle.className = isPassword ? "fas fa-eye-slash" : "fas fa-eye";
        }

        function checkPasswordStrength(password) {
            const strengthBar = document.getElementById('password-strength-bar');
            const strengthText = document.getElementById('password-strength-text');
            let strength = 0;
            
            // Check length
            if (password.length >= 8) strength++;
            if (password.length >= 12) strength++;
            
            // Check for mixed case
            if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
            
            // Check for numbers
            if (/\d/.test(password)) strength++;
            
            // Check for special chars
            if (/[^a-zA-Z0-9]/.test(password)) strength++;
            
            // Update UI
            strengthBar.className = 'strength-' + Math.min(strength, 4);
            
            const messages = ['Very Weak', 'Weak', 'Moderate', 'Strong', 'Very Strong'];
            strengthText.textContent = 'Strength: ' + messages[Math.min(strength, 4)];
        }

        // Validate password match on form submit
        document.getElementById('resetForm').addEventListener('submit', function(e) {
            const password = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            const matchText = document.getElementById('password-match');
            
            if (password !== confirmPassword) {
                e.preventDefault();
                matchText.style.display = 'block';
            }
        });
    </script>
</body>
</html>