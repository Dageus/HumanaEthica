package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class ParticipationDto {
    private Integer id;
    private ActivityDto activityId;
    private UserDto volunteerId;
    private Integer rating;
    private String acceptanceDate;

    public ParticipationDto() {
    }

    public ParticipationDto(Participation participation, boolean deepCopyActivity, boolean deepCopyVolunteer) {
        setId(participation.getId());
        setRating(participation.getRating());
        setAcceptanceDate(DateHandler.toISOString(participation.getAcceptanceDate()));

        if (deepCopyActivity) {
            setActivity(new ActivityDto(participation.getActivity(), false));
        }

        if (deepCopyVolunteer) {
            setVolunteer(new UserDto(participation.getVolunteer()));
        }
    }

    public Integer getId() {
        return id;
    }

    public ActivityDto getActivity() {
        return activityId;
    }

    public UserDto getVolunteer() {
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

    public void setActivity(ActivityDto activityId) {
        this.activityId = activityId;
    }

    public void setVolunteer(UserDto volunteerId) {
        this.volunteerId = volunteerId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setAcceptanceDate(String acceptanceDate) {
        this.acceptanceDate = acceptanceDate;
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