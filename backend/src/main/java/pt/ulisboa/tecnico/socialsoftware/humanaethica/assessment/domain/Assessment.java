package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity.State;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assessment")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String review;

    private LocalDateTime reviewDate;

    // one instituion can have multiple assessments
    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    // one user can have multiple assessments
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Volunteer user;

    public Assessment() {
    }

    public Assessment(Institution institution, Volunteer user, AssessmentDto assessmentDto) {
        setReview(assessmentDto.getReview());
        setReviewDate(LocalDateTime.now());
        setInstitution(institution);
        setUser(user);

        verifyInvariants();
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getReview() {
        return review;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public Volunteer getUser() {
        return user;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        institution.addAssessment(this);
    }

    public void setUser(Volunteer user) {
        this.user = user;
    }

    // Verifications
    private void verifyInvariants() {
        reviewIsRequired();
        instituionIsRequired();
        notAssessedInstitution();
    }

    // review must have at least 10 characters
    private void reviewIsRequired() {
        if (this.review == null || this.review.length() < 10) {
            throw new HEException(ASSESSMENT_REVIEW_INVALID);
        }
    }

    // institution must have at least one activity concluded
    private void instituionIsRequired() {
        // TODO check if activity is considered concluded when state is APPROVED and
        // ending date is before now
        // Add method in activity side, so it can be used here
        // TODO: joao, aqui acho que nao tem de estar aproved, so tem de estar concluido

        this.institution.getActivities().stream()
                .filter(activity -> /*
                                     * activity.getState() == State.APPROVED
                                     * &&
                                     */ activity.getEndingDate().isBefore(LocalDateTime.now()))
                .findAny()
                .orElseThrow(
                        () -> new HEException(ASSESSMENT_INSTITUION_NEEDS_ONE_ACTIVITY, this.institution.getName()));
    }

    // user must not have already assessed the institution
    private void notAssessedInstitution() {
        if (this.institution.getAssessments().stream()
                .anyMatch(assessment -> assessment != this && assessment.getUser().getId() == this.user.getId())) {
            throw new HEException(ASSESSMENT_ALREADY_DONE, this.institution.getName());
        }
        ;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "id=" + id +
                ", review='" + review + '\'' +
                ", reviewDate=" + reviewDate +
                ", institution=" + institution +
                ", user=" + user +
                '}';
    }
}
