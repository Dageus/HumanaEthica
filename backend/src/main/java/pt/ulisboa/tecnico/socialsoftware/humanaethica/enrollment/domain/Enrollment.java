package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String motivation;
    private LocalDateTime enrollmentDateTime;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    public Enrollment(Activity activity, Volunteer volunteer, EnrollmentDto enrollmentDto) {
        setActivity(activity);
        setVolunteer(volunteer);
        setMotivation(enrollmentDto.getMotivation());
        setEnrollmentDateTime(DateHandler.toLocalDateTime(enrollmentDto.getEnrollmentDateTime()));

        verifyInvariants();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addEnrollment(this);
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addEnrollment(this);
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setEnrollmentDateTime(LocalDateTime enrollmentDateTime) {
        this.enrollmentDateTime = enrollmentDateTime;
    }

    public LocalDateTime getEnrollmentDateTime() {
        return enrollmentDateTime;
    }


    private void verifyInvariants() {
        motivationBigEnouth(); // should have more than 10 characters
        volunteerCanOnlyApplyOnce(); // A volunteer can only apply once to an activity
        applicationPeriodIsOpen(); // The application period is open
    }

    private void motivationBigEnouth() {
        if (this.motivation.length() < 10) {
            throw new HEException(MOTIVATION_TOO_SHORT, this.motivation);
        }
    }

    private void volunteerCanOnlyApplyOnce() {
        if (this.activity.getEnrollments().stream()
                .anyMatch(e -> e.getVolunteer().equals(this.volunteer) && e != this)) {
            throw new HEException(VOLUNTEER_ALREADY_ENROLLED, "");
        }
    }    

    private void applicationPeriodIsOpen() {
        if (this.activity.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new HEException(APPLICATION_PERIOD_CLOSED, this.activity.getApplicationDeadline().toString());
        }
    }
}
