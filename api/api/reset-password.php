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
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary: #4361ee;
            --primary-light: #eef2ff;
            --success: #10b981;
            --error: #ef4444;
            --warning: #f59e0b;
            --gray-100: #f3f4f6;
            --gray-200: #e5e7eb;
            --gray-400: #9ca3af;
            --gray-600: #4b5563;
            --gray-800: #1f2937;
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--gray-100);
            color: var(--gray-800);
            line-height: 1.5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            width: 100%;
            max-width: 420px;
        }
        
        .card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
            overflow: hidden;
            transition: all 0.3s ease;
        }
        
        .card-header {
            padding: 32px 32px 24px;
            text-align: center;
            border-bottom: 1px solid var(--gray-200);
        }
        
        .card-header h2 {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 8px;
            color: var(--gray-800);
        }
        
        .card-header p {
            color: var(--gray-600);
            font-size: 15px;
        }
        
        .card-body {
            padding: 24px 32px 32px;
        }
        
        .token-info {
            background-color: var(--primary-light);
            color: var(--primary);
            font-size: 13px;
            padding: 8px 12px;
            border-radius: 6px;
            margin-top: 16px;
            display: inline-block;
            font-weight: 500;
        }
        
        .error-message {
            background-color: rgba(239, 68, 68, 0.1);
            color: var(--error);
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .error-message i {
            font-size: 18px;
        }
        
        .form-group {
            margin-bottom: 20px;
            position: relative;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: 500;
            color: var(--gray-800);
        }
        
        .input-wrapper {
            position: relative;
        }
        
        .form-group input {
            width: 100%;
            padding: 12px 40px 12px 16px;
            border: 1px solid var(--gray-200);
            border-radius: 8px;
            font-size: 15px;
            transition: all 0.2s;
        }
        
        .form-group input:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.15);
        }
        
        .show-password {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            color: var(--gray-400);
            cursor: pointer;
            transition: color 0.2s;
        }
        
        .show-password:hover {
            color: var(--gray-600);
        }
        
        .password-strength {
            height: 4px;
            margin-top: 8px;
            background: var(--gray-200);
            border-radius: 2px;
            overflow: hidden;
        }
        
        .strength-0 { width: 20%; background: var(--error); }
        .strength-1 { width: 40%; background: #ff6b35; }
        .strength-2 { width: 60%; background: var(--warning); }
        .strength-3 { width: 80%; background: #4cc9f0; }
        .strength-4 { width: 100%; background: var(--success); }
        
        .password-hints {
            margin-top: 8px;
            font-size: 12px;
            color: var(--gray-600);
        }
        
        .password-hint {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 4px;
        }
        
        .password-hint i {
            font-size: 12px;
        }
        
        .hint-valid {
            color: var(--success);
        }
        
        .hint-invalid {
            color: var(--gray-400);
        }
        
        .submit-btn {
            width: 100%;
            padding: 14px;
            background-color: var(--primary);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            margin-top: 8px;
        }
        
        .submit-btn:hover {
            background-color: #3a56d4;
        }
        
        .submit-btn:disabled {
            background-color: var(--gray-400);
            cursor: not-allowed;
        }
        
        .success-message {
            display: none;
            flex-direction: column;
            align-items: center;
            text-align: center;
            padding: 32px;
        }
        
        .success-icon {
            width: 64px;
            height: 64px;
            background: var(--success);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 20px;
        }
        
        .success-icon i {
            color: white;
            font-size: 28px;
        }
        
        .success-message h3 {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 12px;
            color: var(--gray-800);
        }
        
        .success-message p {
            color: var(--gray-600);
            margin-bottom: 24px;
            font-size: 15px;
            max-width: 320px;
        }
        
        .app-link {
            display: inline-block;
            padding: 12px 24px;
            background-color: var(--primary-light);
            color: var(--primary);
            border-radius: 8px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.2s;
        }
        
        .app-link:hover {
            background-color: #e0e7ff;
        }
        
        @media (max-width: 480px) {
            .card-header, .card-body {
                padding: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="card">
            <div class="card-header">
                <h2>Reset Password</h2>
                <p>Create a new password for your account</p>
                <?php if ($token): ?>
                    <div class="token-info">
                        <i class="fas fa-clock"></i> Token expires in 15 minutes
                    </div>
                <?php endif; ?>
            </div>
            
            <div class="card-body">
                <?php if ($error): ?>
                    <div class="error-message">
                        <i class="fas fa-exclamation-circle"></i>
                        <?php echo htmlspecialchars($error); ?>
                    </div>
                <?php endif; ?>

                <form id="resetForm" method="POST" <?php echo $success ? 'style="display: none;"' : ''; ?>>
                    <input type="hidden" name="token" value="<?php echo htmlspecialchars($token); ?>">
                    
                    <div class="form-group">
                        <label for="new-password">New Password</label>
                        <div class="input-wrapper">
                            <input type="password" id="new-password" name="password" placeholder="Enter new password" required 
                                   oninput="checkPasswordStrength(this.value)">
                            <span class="show-password" onclick="togglePassword('new-password')">
                                <i class="fas fa-eye"></i>
                            </span>
                        </div>
                        <div class="password-strength">
                            <div id="password-strength-bar" class="strength-0"></div>
                        </div>
                        <div class="password-hints">
                            <div class="password-hint">
                                <i id="length-icon" class="fas fa-circle hint-invalid"></i>
                                <span>At least 8 characters</span>
                            </div>
                            <div class="password-hint">
                                <i id="case-icon" class="fas fa-circle hint-invalid"></i>
                                <span>Uppercase and lowercase letters</span>
                            </div>
                            <div class="password-hint">
                                <i id="number-icon" class="fas fa-circle hint-invalid"></i>
                                <span>At least one number</span>
                            </div>
                            <div class="password-hint">
                                <i id="special-icon" class="fas fa-circle hint-invalid"></i>
                                <span>At least one special character</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="confirm-password">Confirm Password</label>
                        <div class="input-wrapper">
                            <input type="password" id="confirm-password" name="confirm_password" 
                                   placeholder="Confirm new password" required oninput="checkPasswordMatch()">
                            <span class="show-password" onclick="togglePassword('confirm-password')">
                                <i class="fas fa-eye"></i>
                            </span>
                        </div>
                        <div id="password-match" class="password-hint" style="display: none; margin-top: 8px;">
                            <i class="fas fa-exclamation-circle" style="color: var(--error);"></i>
                            <span style="color: var(--error);">Passwords don't match</span>
                        </div>
                    </div>
                    
                    <button type="submit" class="submit-btn" id="submit-btn" disabled>Reset Password</button>
                </form>

                <div class="success-message" id="successMessage" <?php echo $success ? 'style="display: flex;"' : ''; ?>>
                    <div class="success-icon">
                        <i class="fas fa-check"></i>
                    </div>
                    <h3>Password Changed!</h3>
                    <p>Your password has been updated successfully. You can now log in with your new password.</p>
                    <a href="myapp://com.example.myapplication/login" class="app-link">
                        <i class="fas fa-external-link-alt" style="margin-right: 8px;"></i>
                        Open in App
                    </a>
                </div>
            </div>
        </div>
    </div>

    <script>
        function togglePassword(inputId) {
            const input = document.getElementById(inputId);
            const toggle = input.parentElement.querySelector('.show-password i');
            const isPassword = input.type === "password";
            
            input.type = isPassword ? "text" : "password";
            toggle.className = isPassword ? "fas fa-eye-slash" : "fas fa-eye";
        }

        function checkPasswordStrength(password) {
            const strengthBar = document.getElementById('password-strength-bar');
            let strength = 0;
            
            // Check password requirements
            const hasMinLength = password.length >= 8;
            const hasMixedCase = /[a-z]/.test(password) && /[A-Z]/.test(password);
            const hasNumber = /\d/.test(password);
            const hasSpecialChar = /[^a-zA-Z0-9]/.test(password);
            
            // Update requirement icons
            document.getElementById('length-icon').className = hasMinLength ? 
                'fas fa-check-circle hint-valid' : 'fas fa-circle hint-invalid';
            document.getElementById('case-icon').className = hasMixedCase ? 
                'fas fa-check-circle hint-valid' : 'fas fa-circle hint-invalid';
            document.getElementById('number-icon').className = hasNumber ? 
                'fas fa-check-circle hint-valid' : 'fas fa-circle hint-invalid';
            document.getElementById('special-icon').className = hasSpecialChar ? 
                'fas fa-check-circle hint-valid' : 'fas fa-circle hint-invalid';
            
            // Calculate strength
            if (hasMinLength) strength++;
            if (hasMixedCase) strength++;
            if (hasNumber) strength++;
            if (hasSpecialChar) strength++;
            
            // Update strength bar
            strengthBar.className = 'strength-' + Math.min(strength, 4);
            
            // Enable/disable submit button based on password validity
            const submitBtn = document.getElementById('submit-btn');
            const isPasswordValid = hasMinLength && hasMixedCase && hasNumber && hasSpecialChar;
            const passwordsMatch = document.getElementById('confirm-password').value === password;
            
            submitBtn.disabled = !isPasswordValid || !passwordsMatch;
        }
        
        function checkPasswordMatch() {
            const password = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            const matchText = document.getElementById('password-match');
            const submitBtn = document.getElementById('submit-btn');
            
            if (confirmPassword.length > 0 && password !== confirmPassword) {
                matchText.style.display = 'flex';
                submitBtn.disabled = true;
            } else {
                matchText.style.display = 'none';
                
                // Only enable if password is also valid
                const hasMinLength = password.length >= 8;
                const hasMixedCase = /[a-z]/.test(password) && /[A-Z]/.test(password);
                const hasNumber = /\d/.test(password);
                const hasSpecialChar = /[^a-zA-Z0-9]/.test(password);
                const isPasswordValid = hasMinLength && hasMixedCase && hasNumber && hasSpecialChar;
                
                submitBtn.disabled = !isPasswordValid || password !== confirmPassword;
            }
        }

        // Validate form on submit
        document.getElementById('resetForm').addEventListener('submit', function(e) {
            const password = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                document.getElementById('password-match').style.display = 'flex';
            }
        });
    </script>
</body>
</html>