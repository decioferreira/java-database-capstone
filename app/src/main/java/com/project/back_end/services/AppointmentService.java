package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fully qualified: an import would shadow the com.project.back_end.services.Service dependency
@org.springframework.stereotype.Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              Service service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    // The patient always comes from the token, so a patient can't book on someone else's behalf
    @Transactional
    public int bookAppointment(Appointment appointment, String token) {
        try {
            Patient patient = patientFromToken(token);
            Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
            if (patient == null || doctor == null) {
                return 0;
            }
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            log.error("Error booking appointment", e);
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment, String token) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointment existing = appointment.getId() == null ? null
                    : appointmentRepository.findById(appointment.getId()).orElse(null);
            if (existing == null) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Patient patient = patientFromToken(token);
            if (patient == null || !patient.getId().equals(existing.getPatient().getId())) {
                response.put("message", "You can only update your own appointments");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (existing.getStatus() != 0) {
                response.put("message", "Only scheduled appointments can be updated");
                return ResponseEntity.badRequest().body(response);
            }

            // Re-validate the slot only when it changes; the current slot is booked by this very appointment
            boolean slotChanged = !existing.getDoctor().getId().equals(appointment.getDoctor().getId())
                    || !existing.getAppointmentTime().equals(appointment.getAppointmentTime());
            if (slotChanged) {
                int valid = service.validateAppointment(appointment);
                if (valid == -1) {
                    response.put("message", "Invalid doctor id");
                    return ResponseEntity.badRequest().body(response);
                } else if (valid == 0) {
                    response.put("message", "Appointment already booked for given time or doctor not available");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                existing.setDoctor(doctorRepository.getReferenceById(appointment.getDoctor().getId()));
                existing.setAppointmentTime(appointment.getAppointmentTime());
            }

            appointmentRepository.save(existing);
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating appointment {}", appointment.getId(), e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        try {
            Appointment appointment = appointmentRepository.findById(id).orElse(null);
            if (appointment == null) {
                response.put("message", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Patient patient = patientFromToken(token);
            if (patient == null || !patient.getId().equals(appointment.getPatient().getId())) {
                response.put("message", "You can only cancel your own appointments");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            appointmentRepository.delete(appointment);
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling appointment {}", id, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Appointments of the doctor identified by the token, on the given day, optionally filtered by patient name
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointment(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(tokenService.extractEmail(token));
        if (doctor == null) {
            response.put("appointments", List.of());
            return response;
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);
        boolean hasName = pname != null && !pname.isBlank() && !"null".equalsIgnoreCase(pname);

        List<Appointment> appointments = hasName
                ? appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctor.getId(), pname, start, end)
                : appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);

        response.put("appointments", appointments.stream().map(AppointmentDTO::from).toList());
        return response;
    }

    @Transactional
    public void changeStatus(long id, int status) {
        appointmentRepository.updateStatus(status, id);
    }

    private Patient patientFromToken(String token) {
        return patientRepository.findByEmail(tokenService.extractEmail(token));
    }
}
