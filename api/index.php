<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CodeAxe</title>
    <script src="https://unpkg.com/typed.js@2.1.0/dist/typed.umd.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" integrity="sha512-DTOQO9RWCH3ppGqcWaEA1BIZOC6xxalwE7jeAa8E5/mX1bJ+1/py/m3VNfZYFbKgsrq5qPgaL3/4Bu1P+ZQJQ2w==" crossorigin="anonymous" referrerpolicy="no-referrer" />
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Arial', sans-serif;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
            background-color: #0a0a0a; /* Dark hacker background */
            position: relative;
            overflow: hidden;
        }
        body::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, rgba(0, 255, 0, 0.1), rgba(0, 0, 0, 0.9));
            z-index: -1;
            animation: glitch 5s infinite;
        }
        .matrix {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: -1;
            overflow: hidden;
        }

        .matrix span {
            position: absolute;
            color: #00ff00; 
            font-size: 1rem;
            opacity: 0.7;
            animation: fall linear infinite;
            white-space: nowrap;
        }

        .content {
            flex-grow: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            text-align: center;
            padding: 20px;
            position: relative;
            z-index: 1; 
        }

        h1 {
            font-size: 2.5rem;
            color: #db4a2b; 
            margin-bottom: 20px;
            text-shadow: 0 0 10px rgba(219, 74, 43, 0.8); /* Glow effect */
        }

        .typed-text {
            font-weight: bold;
        }

        .icon {
            font-size: 3rem;
            color: #db4a2b;
            margin-bottom: 20px;
            animation: bounce 2s infinite, spin 4s infinite linear;
        }

        /* Footer */
        footer {
            text-align: center;
            padding: 20px;
            background-color: #2c3e50;
            color: white;
            font-size: 0.9rem;
            position: relative;
            z-index: 1;
        }

        /* Animations */
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% {
                transform: translateY(0);
            }
            40% {
                transform: translateY(-20px);
            }
            60% {
                transform: translateY(-10px);
            }
        }

        @keyframes spin {
            0% {
                transform: rotate(0deg);
            }
            100% {
                transform: rotate(360deg);
            }
        }

        @keyframes glitch {
            0% {
                opacity: 0.8;
            }
            20% {
                opacity: 0.6;
            }
            40% {
                opacity: 0.9;
            }
            60% {
                opacity: 0.7;
            }
            100% {
                opacity: 0.8;
            }
        }

        @keyframes fall {
            0% {
                transform: translateY(-100vh);
                opacity: 1;
            }
            100% {
                transform: translateY(100vh);
                opacity: 0;
            }
        }

        /* Responsive Design */
        @media (max-width: 600px) {
            h1 {
                font-size: 1.8rem;
            }
            .icon {
                font-size: 2rem;
            }
            footer {
                font-size: 0.8rem;
            }
            .matrix span {
                font-size: 0.8rem;
            }
        }
    </style>
</head>
<body>
    <div class="matrix"></div>
    <div class="content">
        <i class="fas fa-code icon"></i>
        <h1><span class="typed-text"></span></h1>
    </div>
    <script>
        var typed = new Typed('.typed-text', {
            strings: ["CodeAxe - This is a demo website for learning."],
            typeSpeed: 50,
            backSpeed: 20,
            loop: true,
            showCursor: true,
            cursorChar: '|',
        });

        const matrix = document.querySelector('.matrix');
        function createMatrixChar() {
            const span = document.createElement('span');
            span.textContent = String.fromCharCode(33 + Math.random() * 94); 
            span.style.left = Math.random() * 100 + 'vw'; 
            span.style.animationDuration = 2 + Math.random() * 3 + 's'; 
            matrix.appendChild(span);
            setTimeout(() => span.remove(), 5000); 
        }
        setInterval(createMatrixChar, 100);
    </script>
</body>
</html>