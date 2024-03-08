package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@DataJpaTest
class GetParticipationsByActivityServiceTest extends SpockTest {
    private Activity activity

    def setup() {
        def institution = institutionService.getDemoInstitution()
        given: "activity info"
        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,3,ACTIVITY_DESCRIPTION_1,
                ONE_DAY_AGO,IN_TWO_DAYS,IN_THREE_DAYS,null)
        and: "a theme"
        def themes = new ArrayList<>()
        themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))
        and: "an activity"
        activity = new Activity(activityDto, institution, themes)
        activity = activityRepository.save(activity)
        and: "user info"
        def volunteer_1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)  
        and: "another user info"
        def volunteer_2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        and: "participation info"
        def participationDto_1 = createParticipationDto(RATING_1, NOW, null, null)
        and: "another participation info"
        def participationDto_2 = createParticipationDto(RATING_2, NOW, null, null)
        and: "a participation"
        def participation_1 = new Participation(activity, volunteer_1, participationDto_1)
        participationRepository.save(participation_1)
        and: "another participation"
        def participation_2 = new Participation(activity, volunteer_2, participationDto_2)
        participationRepository.save(participation_2)
    }

    def 'get two participation'() {
        when:
        def result = participationService.getParticipationsByActivity(activity.id)

        then:
        result.size() == 2
        result.get(0).rating == RATING_1
        result.get(1).rating == RATING_2
    }

    def 'get participations with invalid activityId throws exception'() {
        when:
        participationService.getParticipationsByActivity(-1)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_ID_INVALID
    }

    def 'get participations with null activityId throws exception'() {
        when:
        participationService.getParticipationsByActivity(null)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_ID_NULL
    }

    def 'get participations with activity that does not exist'() {
        when:
        participationService.getParticipationsByActivity(2)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_NOT_FOUND
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
