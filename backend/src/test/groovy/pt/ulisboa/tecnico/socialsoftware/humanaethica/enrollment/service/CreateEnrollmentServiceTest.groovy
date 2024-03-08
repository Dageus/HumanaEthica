package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*

@DataJpaTest
class CreateEnrollmentServiceTest extends SpockTest {
  def activity
  def volunteer
  def setup() {
    def institution = institutionService.getDemoInstitution()
    given: "activity info"
    def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
            IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,null)
    and: "a theme"
    def themes = new ArrayList<>()
    themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))
    and: "an activity"
    activity = new Activity(activityDto, institution, themes)
    activityRepository.save(activity)
    and: "user info"
    volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)  
    and: "enrollment info"
  }

  def 'create enrollment'() {
    when:
    def result = enrollmentService.createEnrollment(volunteer.id, activity.id, createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null))

    then:
    result.volunteer.name == USER_1_NAME
    result.activity.name == ACTIVITY_NAME_1
    result.motivation == ENROLLMENT_MOTIVATION_1
  }

  def 'create enrollment with invalid volunteerId throws exception'() {
    when:
    enrollmentService.createEnrollment(-1, activity.id, createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null))
    
    then:
    def exception = thrown(HEException)
    exception.getErrorMessage() == USER_NOT_FOUND
  }

    def 'create enrollment with null volunteerId throws exception'() {
    when:
    enrollmentService.createEnrollment(null, activity.id, createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null))
    
    then:
    def exception = thrown(HEException)
    exception.getErrorMessage() == USER_NOT_FOUND
  }

  def 'create enrollment with invalid activityId throws exception'() {
    when:
    enrollmentService.createEnrollment(volunteer.id, -1, createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null))
    
    then:
    def exception = thrown(HEException)
    exception.getErrorMessage() == ACTIVITY_ID_INVALID
  }

  def 'create enrollment with null activityId throws exception'() {
    when:
    enrollmentService.createEnrollment(volunteer.id, null, createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null))
    
    then:
    def exception = thrown(HEException)
    exception.getErrorMessage() == ACTIVITY_ID_NULL
  }

  @TestConfiguration
  static class LocalBeanConfiguration extends BeanConfiguration {}
}