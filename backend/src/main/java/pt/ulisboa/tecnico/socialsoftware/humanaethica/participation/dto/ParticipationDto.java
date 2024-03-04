package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.util.List;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;

public class ParticipationDto {
    private Integer id;
    private Integer activityId;
    private Integer volunteerId;
    private Integer rating;
    private String acceptanceDate;

    public ParticipationDto() {
    }

    public ParticipationDto(Participation participation, boolean deepCopyActivity, boolean deepCopyVolunteer) {
        setId(participation.getId());
        setActivityId(participation.getActivity().getId());
        setVolunteerId(participation.getVolunteer().getId());
        setRating(participation.getRating());
        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));
    }

    public Integer getId() {
        return id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public Integer getVolunteerId() {
        return volunteerId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getAcceptanceDate() {
        return acceptanceDate;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
    }

    public ParticipationDto createParticipation(@PathVariable Integer activityId, @Valid @RequestBody ParticipationDto participationDto) {
        
    }

    public List<ParticipationDto> getActivityParticipations(@PathVariable Integer activityId) {

    }

    @Override
    public String toString() {
        return "ParticipationDto{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", volunteerId=" + volunteerId +
                ", rating=" + rating +
                ", acceptanceDate='" + acceptanceDate + '\'' +
                '}';
    }
}