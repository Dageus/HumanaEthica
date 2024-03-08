package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.dto.ThemeDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.InstitutionService
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import spock.lang.Unroll



@DataJpaTest
class CreateAssessmentServiceTest extends SpockTest {
  def institution
  def volunteer
  def assessmentDto
  def activity

  def setup(){
    given: "institution info"
    //create instution dto
    institution = institutionService.getDemoInstitution()

    and: "theme info"
    def themes = new ArrayList<>()
    themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED, null))

    and: "activity info"
    def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
    IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,null)

    activity = new Activity(activityDto, institution, themes)

    //change activity deadline
    activity.setEndingDate(TWO_DAYS_AGO)
    activityRepository.save(activity)

    institution.addActivity(activity) 

    //create volunteerDto
    volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE) 

    assessmentDto = createAssessmentDto(ASSESSMENT_VALID_REVIEW)
  }

  def "create assessment with valid parameters"() {
    when:
    def assessment = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto)

    then:
    notThrown(HEException)
    assessment.getUser() == volunteer.getId()
    assessment.getInstitution() == institution.getId()
    assessment.getReview() == ASSESSMENT_VALID_REVIEW
  }

  @Unroll
  def "create assessment with invalid volunteerId: volunteerId=#volunteerId"() {
    when:
    def assessment = assessmentService.createAssessment(volunteerId, institution.getId(), assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.USERID_NOT_VALID

    where:
    volunteerId << [null, -1, 0]
  }

  @Unroll
  def "create assessment with invalid institutionId: institutionId=#institutionId"() {
    when:
    def assessment = assessmentService.createAssessment(volunteer.getId(), institutionId, assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.INSTITUTIONID_NOT_VALID

    where:
    institutionId << [null, -1, 0]
  }

  def "create assessment with non existing institution id"() {
    when:
    def assessment = assessmentService.createAssessment(volunteer.getId(), 222, assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.INSTITUTIONID_NOT_FOUND
  }

  def "create assessment with non existing volunteer id"() {
    when:
    def assessment = assessmentService.createAssessment(222, institution.getId(), assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.USERID_NOT_FOUND
  }

  @TestConfiguration
  static class LocalBeanConfiguration extends BeanConfiguration {}
}
