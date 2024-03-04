package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

public class VolunteerDto extends UserDto {
    private List<EnrollmentDto> enrollments;

    public VolunteerDto() {
    }

    public VolunteerDto(Volunteer volunteer, boolean deepCopyEnrollments) {
        super(volunteer);

        if (deepCopyEnrollments && (volunteer.getEnrollments() != null)) {
            List<EnrollmentDto> enrollments = new ArrayList<>();

            for (Enrollment enrollment : volunteer.getEnrollments()) {
                enrollments.add(new EnrollmentDto(enrollment, false, false));
            }
            setEnrollments(enrollments);
        }
    }

    public List<EnrollmentDto> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<EnrollmentDto> enrollments) {
        this.enrollments = enrollments;
    }
}