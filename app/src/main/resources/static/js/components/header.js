// header.js

function renderHeader() {
  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  const path = window.location.pathname;
  if (path.endsWith("/") || path.endsWith("/index.html")) {
    localStorage.removeItem("userRole");
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="/assets/images/logo/logo.png" alt="Hospital CMS Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  let headerContent = `
    <header class="header">
      <div class="logo-section">
        <img src="/assets/images/logo/logo.png" alt="Hospital CMS Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav>`;

  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  } else if (role === "admin") {
    headerContent += `
        <button id="addDocBtn" class="adminBtn">Add Doctor</button>
        <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "doctor") {
    headerContent += `
        <button class="adminBtn" onclick="selectRole('doctor')">Home</button>
        <a href="#" onclick="logout()">Logout</a>`;
  } else if (role === "patient") {
    headerContent += `
        <button id="patientLogin" class="adminBtn">Login</button>
        <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === "loggedPatient") {
    headerContent += `
        <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
        <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
        <a href="#" onclick="logoutPatient()">Logout</a>`;
  }

  headerContent += `
      </nav>
    </header>`;

  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
}

// openModal lives in an ES module, so it's only reachable here if a page module exposes it on window
function attachHeaderButtonListeners() {
  if (typeof window.openModal !== "function") return;

  const adminLogin = document.getElementById("adminLogin");
  if (adminLogin) {
    adminLogin.addEventListener("click", () => window.openModal("adminLogin"));
  }

  const doctorLogin = document.getElementById("doctorLogin");
  if (doctorLogin) {
    doctorLogin.addEventListener("click", () => window.openModal("doctorLogin"));
  }
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient() {
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

renderHeader();
