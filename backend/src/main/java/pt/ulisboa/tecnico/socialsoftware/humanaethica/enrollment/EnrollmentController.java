package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/activities/{activityId}/apply")
    @PreAuthorize("hasRole('ROLE_VOLUNTEER')")
    public EnrollmentDto createEnrollment(
            Principal principal,
            @PathVariable Integer activityId,
            @Valid @RequestBody EnrollmentDto enrollmentDto) {
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return enrollmentService.createEnrollment(userId, activityId, enrollmentDto);
    }

    // List all enrollments of an activity
    @GetMapping("/activities/{activityId}/enrollments")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public List<EnrollmentDto> getActivityEnrollments(@PathVariable Integer activityId) {
        return enrollmentService.getEnrollmentsByActivity(activityId);
    }
}
