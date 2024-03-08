package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime



@DataJpaTest
class CreateAssessmentMethodTest extends SpockTest {
  Institution institution = Mock()
  Volunteer volunteer = Mock()
  Activity activity = Mock()
  Assessment otherAssessment = Mock()

  def assessmentDto

  def setup() {
    given: "assessment info"
    assessmentDto = new AssessmentDto()
    assessmentDto.review = "This is a valid review with more than 10 characters."
  }

  def "create assessment successfully"() {
    given:
    activity.endingDate >> ONE_DAY_AGO
    institution.getActivities() >> [activity]
    institution.getAssessments() >> []

    when: "create assessment"
    def assessment = new Assessment(institution, volunteer, assessmentDto)
    
    then: "verify assessment data"
    assessment.getInstitution() == institution
    assessment.getUser() == volunteer
    assessment.getReview() == assessmentDto.review

    notThrown(HEException)
  }

  def "create assessment with activity to be concluded"() {
    given:
    activity.endingDate >> IN_ONE_DAY
    institution.getActivities() >> [activity]
    institution.getAssessments() >> []

    when:
    new Assessment(institution, volunteer, assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.ASSESSMENT_INSTITUION_NEEDS_ONE_ACTIVITY
  }

  def "create assessment without activities"(){
    given:
    institution.getActivities() >> []
    institution.getAssessments() >> []

    when:
    new Assessment(institution, volunteer, assessmentDto)

    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.ASSESSMENT_INSTITUION_NEEDS_ONE_ACTIVITY
  }

  @Unroll
  def "create assessment with invalid review : review=#review"() {
    given:
    activity.endingDate >> ONE_DAY_AGO
    institution.getActivities() >> [activity]
    institution.getAssessments() >> []
    assessmentDto.review = review

    when:
    new Assessment(institution, volunteer, assessmentDto)

    then: 
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.ASSESSMENT_REVIEW_INVALID

    where:
    review << [null, 1, "Short"]
  }

  def "create assessment and violate volunteer can only review an institution once"() {
    given:
    volunteer.getId() >> 1
    otherAssessment.getUser() >> volunteer

    activity.endingDate >> ONE_DAY_AGO
    institution.getActivities() >> [activity]
    institution.getAssessments() >> [otherAssessment]

    when: "create assessment with same volunteer"
    new Assessment(institution, volunteer, assessmentDto) 
 
    then:
    def error = thrown(HEException)
    error.getErrorMessage() == ErrorMessage.ASSESSMENT_ALREADY_DONE
  }

  @TestConfiguration
  static class LocalBeanConfiguration extends BeanConfiguration {}
}