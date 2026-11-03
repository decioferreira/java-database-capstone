// doctorDashboard.js
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

const tableBody = document.getElementById("patientTableBody");
let selectedDate = formatDate(new Date());
const token = localStorage.getItem("token");
let patientName = null;

// Local date in YYYY-MM-DD (toISOString would use UTC and can be off by a day)
function formatDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

document.getElementById("searchBar").addEventListener("input", (event) => {
  const value = event.target.value.trim();
  patientName = value.length > 0 ? value : "null";
  loadAppointments();
});

document.getElementById("todayButton").addEventListener("click", () => {
  selectedDate = formatDate(new Date());
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();
});

document.getElementById("datePicker").addEventListener("change", (event) => {
  selectedDate = event.target.value;
  loadAppointments();
});

async function loadAppointments() {
  try {
    const response = await getAllAppointments(selectedDate, patientName, token);
    const appointments = response.appointments || [];

    tableBody.innerHTML = "";

    if (appointments.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">No Appointments found for today.</td></tr>`;
      return;
    }

    appointments.forEach(appointment => {
      // Supports both the flat AppointmentDTO shape and an Appointment entity with a nested patient
      const patient = {
        id: appointment.patientId ?? appointment.patient?.id,
        name: appointment.patientName ?? appointment.patient?.name,
        phone: appointment.patientPhone ?? appointment.patient?.phone,
        email: appointment.patientEmail ?? appointment.patient?.email
      };
      const doctorId = appointment.doctorId ?? appointment.doctor?.id;
      tableBody.appendChild(createPatientRow(patient, appointment.id, doctorId));
    });
  } catch (error) {
    console.error("Error loading appointments:", error);
    tableBody.innerHTML = `<tr><td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td></tr>`;
  }
}

document.addEventListener("DOMContentLoaded", () => {
  renderContent();
  document.getElementById("datePicker").value = selectedDate;
  loadAppointments();
});
