package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

/*
 * 
 * Um membro da instituição associa um voluntário como participante de uma atividade, em que a criação pode conter o valor da avaliação;
 * 
 * Um membro da instituição obtém a lista de todas as participações numa atividade.
 * 
 */

@Entity
@Table(name = "participation")
public class Participation {
    // Instance variables

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;
    
    private Integer rating;

    @Column(name = "creation_date")
    private LocalDateTime acceptanceDate;

    public Participation() {

    }

    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto) {
        setActivity(activity);
        setVolunteer(volunteer);
        setRating(participationDto.getRating());    
        setAcceptanceDate(DateHandler.toLocalDateTime(participationDto.getAcceptanceDate()));

        // volunteer.addParticipation(this);
        // activity.addParticipation(this);

        verifyInvariants();
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public Integer getRating() {
        return rating;
    }

    public LocalDateTime getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        activity.addParticipation(this);
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addParticipation(this);
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setAcceptanceDate(LocalDateTime acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    // Add more methods as needed
    private void activityIsRequired() {
        if (this.activity == null) {
            throw new HEException(ACTIVITY_DOES_NOT_EXIST);
        }
    }

    private void volunteerIsRequired() {
        if (this.volunteer == null) {
            throw new HEException(VOLUNTEER_DOES_NOT_EXIST);
        }
    }

    private void ratingIsRequired() {
        if (this.rating == null) {
            throw new HEException(RATING_DOES_NOT_EXIST);
        }
    }

    private void acceptanceDateIsRequired() {
        if (this.acceptanceDate == null) {
            throw new HEException(ACCEPTANCE_DATE_DOES_NOT_EXIST);
        }
    }

    private void acceptanceDateCannotBeInTheFuture() {
        if (this.acceptanceDate.isAfter(LocalDateTime.now())) {
            throw new HEException(ACCEPTANCE_DATE_CANNOT_BE_IN_THE_FUTURE);
        }
    }

    private void verifyInvariants() {
        activityIsRequired();
        volunteerIsRequired();
        ratingIsRequired();
        acceptanceDateIsRequired();
        //acceptanceDateCannotBeInTheFuture();
        participantsMoreThanLimit();
        volunteerAlreadyParticipated();
        enrollmentProcessOngoing();
    }

    private void participantsMoreThanLimit() {
        if (activity.getParticipations().size() >= activity.getParticipantsNumberLimit()) {
            throw new HEException(ACTIVITY_PARTICIPANTS_EXCEED_LIMIT);
        }
    }

    private void volunteerAlreadyParticipated() {
        if (activity.getParticipations().stream().anyMatch(participation -> participation.getVolunteer().equals(volunteer) && participation != this)) {
            throw new HEException(VOLUNTEER_ALREADY_PARTICIPATED);
        }
    }

    private void enrollmentProcessOngoing() {
        if (activity.getApplicationDeadline().isAfter(LocalDateTime.now())) {
            throw new HEException(ENROLLMENT_PROCESS_ONGOING);
        }
    }
}