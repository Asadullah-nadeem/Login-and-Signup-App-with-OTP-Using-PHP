<?php
function generateOTP() {
    return str_pad(mt_rand(0, 9999), 4, '0', STR_PAD_LEFT);
}

function validateOTP($otp) {
    return preg_match('/^\d{4}$/', $otp);
}
?>