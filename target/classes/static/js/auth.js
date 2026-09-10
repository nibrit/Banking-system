const API_BASE_URL = "http://localhost:8080";

const loginForm = document.getElementById("loginForm");
const loginMessage = document.getElementById("loginMessage");

loginForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    loginMessage.textContent = "Logging in...";

    try {

        const response = await fetch(
            `${API_BASE_URL}/auth/login`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        const data = await response.json();

        if (!response.ok) {

            loginMessage.textContent =
                data.message || "Invalid email or password";

            return;
        }

        // Store JWT in browser
        localStorage.setItem("jwtToken", data.token);

        // Store email for dashboard use
        localStorage.setItem("userEmail", email);

        // Go to dashboard
        window.location.href = "/dashboard.html";

    } catch (error) {

        console.error("Login error:", error);

        loginMessage.textContent =
            "Unable to connect to the server.";
    }
});