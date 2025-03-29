const API_BASE_URL = 'http://codeaxe.co.in/api';

function showError(message) {
    const banner = document.getElementById('errorBanner');
    banner.textContent = message;
    banner.style.display = 'block';
    setTimeout(() => {
        banner.style.display = 'none';
    }, 5000);
}

async function tryEndpoint(endpoint) {
    const responseViewer = document.getElementById(`${endpoint}Response`);
    responseViewer.style.display = 'none';
    
    try {
        const sampleData = {
            signup: {
                full_name: "John Doe",
                email: "john@example.com",
                password: "securePassword123"
            },
            login: {
                email: "john@example.com",
                password: "securePassword123"
            }
        };

        const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(sampleData[endpoint])
        });

        const data = await response.json();
        
        responseViewer.innerHTML = `<pre>${JSON.stringify(data, null, 4)}</pre>`;
        responseViewer.style.display = 'block';
        
        if (!response.ok) {
            responseViewer.style.borderColor = '#dc2626';
            showError(data.message || 'An error occurred');
        } else {
            responseViewer.style.borderColor = '#16a34a';
        }
    } catch (error) {
        showError('Network error - Please check your connection');
        console.error('Error:', error);
    }
}

// Live Form Handling
document.addEventListener('DOMContentLoaded', () => {
    const forms = document.querySelectorAll('.live-form');
    
    forms.forEach(form => {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const responseViewer = form.querySelector('.response-viewer');
            
            try {
                const response = await fetch(form.action, {
                    method: form.method,
                    body: JSON.stringify(Object.fromEntries(formData)),
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                
                const data = await response.json();
                responseViewer.innerHTML = `<pre>${JSON.stringify(data, null, 4)}</pre>`;
                
                if (!response.ok) {
                    showError(data.message || 'Request failed');
                }
            } catch (error) {
                showError('Request failed');
            }
        });
    });
});