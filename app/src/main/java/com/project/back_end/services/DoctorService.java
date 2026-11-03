package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);
    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // Slots look like "09:00-10:00"; a slot is booked when an appointment starts at its start time
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null || doctor.getAvailableTimes() == null) {
            return List.of();
        }

        Set<String> bookedStarts = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime().format(SLOT_FORMAT))
                .collect(Collectors.toSet());

        return doctor.getAvailableTimes().stream()
                .filter(slot -> !bookedStarts.contains(slotStart(slot)))
                .toList();
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            log.error("Error saving doctor", e);
            return 0;
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            if (doctor.getId() == null || !doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            log.error("Error updating doctor {}", doctor.getId(), e);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            log.error("Error deleting doctor {}", id, e);
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());
            if (doctor == null || !doctor.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(doctor.getEmail()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating doctor login", e);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        return doctorsResponse(doctorRepository.findByNameLike(name));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        return doctorsResponse(filterDoctorByTime(doctors, amOrPm));
    }

    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
                .filter(doctor -> doctor.getAvailableTimes() != null && doctor.getAvailableTimes().stream()
                        .anyMatch(slot -> matchesPeriod(slot, amOrPm)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        return doctorsResponse(filterDoctorByTime(doctorRepository.findByNameLike(name), amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        return doctorsResponse(doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        return doctorsResponse(filterDoctorByTime(doctorRepository.findBySpecialtyIgnoreCase(specialty), amOrPm));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        return doctorsResponse(doctorRepository.findBySpecialtyIgnoreCase(specialty));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        return doctorsResponse(filterDoctorByTime(doctorRepository.findAll(), amOrPm));
    }

    private static String slotStart(String slot) {
        return slot.split("-")[0].trim();
    }

    // AM = slot starts before noon, PM = slot starts at or after noon
    private static boolean matchesPeriod(String slot, String amOrPm) {
        try {
            boolean isAm = LocalTime.parse(slotStart(slot), SLOT_FORMAT).isBefore(LocalTime.NOON);
            return "AM".equalsIgnoreCase(amOrPm) ? isAm : "PM".equalsIgnoreCase(amOrPm) && !isAm;
        } catch (Exception e) {
            return false;
        }
    }

    private static Map<String, Object> doctorsResponse(List<Doctor> doctors) {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return response;
    }
}
