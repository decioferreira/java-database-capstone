package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            log.error("Error creating patient", e);
            return 0;
        }
    }

    // Patients may only read their own appointments; doctors only see the ones they are assigned to
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String user, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractEmail(token);
            List<Appointment> appointments = appointmentRepository.findByPatientId(id);

            if ("patient".equals(user)) {
                Patient patient = patientRepository.findByEmail(email);
                if (patient == null || !patient.getId().equals(id)) {
                    response.put("message", "Unauthorized access to another patient's appointments");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {
                appointments = appointments.stream()
                        .filter(appointment -> email.equals(appointment.getDoctor().getEmail()))
                        .toList();
            }

            response.put("appointments", toDTOs(appointments));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching appointments for patient {}", id, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        Integer status = statusFor(condition);
        if (status == null) {
            response.put("message", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            response.put("appointments", toDTOs(appointments));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error filtering appointments by condition for patient {}", id, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
            response.put("appointments", toDTOs(appointments));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error filtering appointments by doctor for patient {}", patientId, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        Integer status = statusFor(condition);
        if (status == null) {
            response.put("message", "Invalid condition. Use 'past' or 'future'");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
            response.put("appointments", toDTOs(appointments));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error filtering appointments by doctor and condition for patient {}", patientId, e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(tokenService.extractEmail(token));
            if (patient == null) {
                response.put("message", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("patient", patient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching patient details", e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // "future" = scheduled (status 0), "past" = completed (status 1)
    private static Integer statusFor(String condition) {
        if ("future".equalsIgnoreCase(condition)) return 0;
        if ("past".equalsIgnoreCase(condition)) return 1;
        return null;
    }

    private static List<AppointmentDTO> toDTOs(List<Appointment> appointments) {
        return appointments.stream().map(AppointmentDTO::from).toList();
    }
}
