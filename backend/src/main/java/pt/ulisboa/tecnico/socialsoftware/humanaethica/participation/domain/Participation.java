package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;

import java.util.List;

import jakarta.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.ACTIVITY_NAME_INVALID;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Activity activity;

    @ManyToOne
    private Volunteer volunteer;

    private Integer rating;
    private LocalDateTime acceptanceDate;

    public Participation() {

    }

    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto) {
        setActivity(activity);
        setVolunteer(volunteer);
        setRating(null);    
        setAcceptanceDate(LocalDateTime.now());

        verifyInvariants();
    }

    // Constructor
    public Participation(Activity activity, Volunteer volunteer, ParticipationDto participationDto, Integer rating) {
        setActivity(activity);
        setVolunteer(volunteer);
        setRating(rating);
        setAcceptanceDate(LocalDateTime.now());

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
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
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
        acceptanceDateCannotBeInTheFuture();
    }

}