package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.repository.ActivityRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.repository.EnrollmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class EnrollmentService {
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    UserRepository userRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<EnrollmentDto> getEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(enrollment -> new EnrollmentDto(enrollment, true, true))
                .sorted(Comparator.comparing(EnrollmentDto::getId))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EnrollmentDto createEnrollment(Integer userId, Integer activityId, EnrollmentDto enrollmentDto) {
        if (activityId == null)
            throw new HEException(ACTIVITY_NOT_FOUND);
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        if (userId == null)
            throw new HEException(USER_NOT_FOUND);
        Volunteer volunteer = (Volunteer) userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        Enrollment enrollment = new Enrollment(activity, volunteer, enrollmentDto);

        return new EnrollmentDto(enrollment, true, true);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<EnrollmentDto> getEnrollmentsByActivity(Integer activityId) {
        if (activityId == null)
            throw new HEException(ACTIVITY_ID_NULL);
        if (activityId <= 0) {
            throw new HEException(ACTIVITY_ID_INVALID, activityId);
        }

        activityRepository.findById(activityId).orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        List<Enrollment> enrollments = enrollmentRepository.getEnrollmentsByActivityId(activityId);

        List<EnrollmentDto> enrollmentsDto = enrollments.stream()
                .map(enrollment -> {
                    return new EnrollmentDto(enrollment, true, true);
                }).collect(Collectors.toList());

        return enrollmentsDto;
    }
}
