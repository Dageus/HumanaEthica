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
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*

@DataJpaTest
class CreateParticipationServiceTest extends SpockTest {

  def activity

  def setup() {
    def institution = institutionService.getDemoInstitution()
    given: "activity info"
    def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,3,ACTIVITY_DESCRIPTION_1,
            IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,null)
    and: "a theme"
    def themes = new ArrayList<>()
    themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))
    and: "an activity"
    activity = new Activity(activityDto, institution, themes)
    activityRepository.save(activity)
  }

  // def 'create participation'() {
  //   when:
  //   def result = participationService.createParticipation(activity.id, createParticipationDto(RATING_1, NOW, null, volunteerDto))

  //   then:
  //   result.volunteer.name == USER_1_NAME
  //   result.activity.name == ACTIVITY_NAME_1
  //   result.rating == RATING_1
  // }

  def 'create participation with invalid activityId throws exception'() {
    when:
    participationService.createParticipation(-1, createParticipationDto(RATING_1, NOW, null, null))
    
    then:
    def exception = thrown(HEException)
    exception.getErrorMessage() == ACTIVITY_ID_INVALID
  }

  @TestConfiguration
  static class LocalBeanConfiguration extends BeanConfiguration {}
}