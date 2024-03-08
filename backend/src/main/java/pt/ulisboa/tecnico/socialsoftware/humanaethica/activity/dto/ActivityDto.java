package pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.util.ArrayList;
import java.util.List;

public class ActivityDto {
    private Integer id;
    private String name;
    private String region;
    private Integer participantsNumberLimit;
    private String description;
    private String startingDate;
    private String endingDate;
    private String applicationDeadline;
    private String state;
    private String creationDate;
    private List<ThemeDto> themes;
    private List<EnrollmentDto> enrollments;
    private InstitutionDto institution;

    public ActivityDto() {
    }

    public ActivityDto(Activity activity, boolean deepCopyInstitution, boolean deepCopyEnrollments) {
        setId(activity.getId());
        setName(activity.getName());
        setRegion(activity.getRegion());
        setParticipantsNumberLimit(activity.getParticipantsNumberLimit());
        setDescription(activity.getDescription());

        this.themes = activity.getThemes().stream()
                .map(theme -> new ThemeDto(theme, false, true, false))
                .toList();

        setState(activity.getState().name());
        setCreationDate(DateHandler.toISOString(activity.getCreationDate()));
        setStartingDate(DateHandler.toISOString(activity.getStartingDate()));
        setEndingDate(DateHandler.toISOString(activity.getEndingDate()));
        setApplicationDeadline(DateHandler.toISOString(activity.getApplicationDeadline()));

        if (deepCopyInstitution && (activity.getInstitution() != null)) {
            setInstitution(new InstitutionDto(activity.getInstitution(), false, false));
        }

        if (deepCopyEnrollments && (activity.getEnrollments() != null)) {
            List<EnrollmentDto> enrollments = new ArrayList<>();

            for (Enrollment enrollment : activity.getEnrollments()) {
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

    public void setThemes(List<ThemeDto> themes) {
        this.themes = themes;
    }

    public List<ThemeDto> getThemes() {
        return themes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public String getEndingDate() {
        return endingDate;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public InstitutionDto getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDto institution) {
        this.institution = institution;
    }

    public Integer getParticipantsNumberLimit() {
        return participantsNumberLimit;
    }

    public void setParticipantsNumberLimit(Integer participantsNumberLimit) {
        this.participantsNumberLimit = participantsNumberLimit;
    }

    @Override
    public String toString() {
        return "ActivityDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", participantsNumber=" + participantsNumberLimit +
                ", description='" + description + '\'' +
                ", startingDate='" + startingDate + '\'' +
                ", endingDate='" + endingDate + '\'' +
                ", applicationDeadline='" + applicationDeadline + '\'' +
                ", state='" + state + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", themes=" + themes +
                ", institution=" + institution +
                '}';
    }
}
