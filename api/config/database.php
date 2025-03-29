<?php
$host = 'localhost';
$dbname = 'codeaxe5_api_v1';
$username = 'codeaxe5_Config_v1';
$password = '4(n,UxQ153{b';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    die("Database connection failed: " . $e->getMessage());
}
?>