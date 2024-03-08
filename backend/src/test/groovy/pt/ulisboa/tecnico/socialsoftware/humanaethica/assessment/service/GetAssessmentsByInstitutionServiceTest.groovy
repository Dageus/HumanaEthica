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
class GetAssessmentsByInstitutionServiceTest extends SpockTest {
  def institution
  def volunteer
  def activity
  def assessmentDto
  def assessment

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
    assessment = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto)
  }

  def "get assessment by institution with valid institution id"() {
    when:
    def res = assessmentService.getAssessmentsByInstitution(institution.getId())

    then:
    notThrown(HEException)
    res.size() == 1
    res[0].getId() == assessment.getId()
    res[0].getReview() == ASSESSMENT_VALID_REVIEW
    res[0].getInstitution() == institution.getId()
    res[0].getUser() == volunteer.getId()
  }

  def "get assessment by institution with non existing institution id"() {
    when:
    assessmentService.getAssessmentsByInstitution(222)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.INSTITUTIONID_NOT_FOUND
  }

  @Unroll
  def "get assessment by institution with invalid institution id: institutionId=#institutionId"() {
    when:
    assessmentService.getAssessmentsByInstitution(institutionId)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.INSTITUTIONID_NOT_VALID

    where:
    institutionId << [null, -1, 0]
  }


  @TestConfiguration
  static class LocalBeanConfiguration extends BeanConfiguration {}
}
