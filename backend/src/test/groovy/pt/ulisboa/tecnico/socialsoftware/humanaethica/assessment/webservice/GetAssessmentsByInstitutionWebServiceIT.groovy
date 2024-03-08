package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.http.HttpStatus
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import spock.lang.Unroll



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetAssessmentByInstitutionWebServiceIT extends SpockTest {
  @LocalServerPort
  private int port

  def institution
  def volunteer
  def assessmentDto
  def assessment

  def setup(){
    deleteAll()

    webClient = WebClient.create("http://localhost:" + port)
    headers = new HttpHeaders()
    headers.setContentType(MediaType.APPLICATION_JSON)

    given: "institution info"
    //create instution dto
    institution = institutionService.getDemoInstitution()

    and: "theme info"
    def themes = new ArrayList<>()
    themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED, null))

    and: "activity info"
    def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
    IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,null)

    def activity = new Activity(activityDto, institution, themes)

    //change activity deadline
    activity.setEndingDate(TWO_DAYS_AGO)
    activityRepository.save(activity)

    institution.addActivity(activity) 

    //create volunteerDto
    volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE) 

    assessmentDto = createAssessmentDto(ASSESSMENT_VALID_REVIEW)
    
    assessment = assessmentService.createAssessment(volunteer.getId(), institution.getId(), assessmentDto)
  }

  def "get assessment without login"() {
    when:
    def response = webClient.get()
      .uri('/assessment/' + institution.getId())
      .headers(httpHeaders -> httpHeaders.putAll(headers))
      .bodyValue(assessmentDto)
      .retrieve()
      .bodyToFlux(AssessmentDto.class)
      .collectList()
      .block()

    then: "check response"
    response != null
    response.get(0).getUser() == volunteer.getId()
    response.get(0).getInstitution() == institution.getId()
    response.get(0).getReview() == ASSESSMENT_VALID_REVIEW
  }

  def "get assessment with member login"() {
    given:
    demoMemberLogin()

    when:
    def response = webClient.get()
      .uri('/assessment/' + institution.getId())
      .headers(httpHeaders -> httpHeaders.putAll(headers))
      .bodyValue(assessmentDto)
      .retrieve()
      .bodyToFlux(AssessmentDto.class)
      .collectList()
      .block()

    then: "check response"
    response != null
    response.get(0).getUser() == volunteer.getId()
    response.get(0).getInstitution() == institution.getId()
    response.get(0).getReview() == ASSESSMENT_VALID_REVIEW
  }

  def "get assessment with volunteer login"() {
    given:
    demoVolunteerLogin()

    when:
    def response = webClient.get()
      .uri('/assessment/' + institution.getId())
      .headers(httpHeaders -> httpHeaders.putAll(headers))
      .bodyValue(assessmentDto)
      .retrieve()
      .bodyToFlux(AssessmentDto.class)
      .collectList()
      .block()

    then: "check response"
    response != null
    response.get(0).getUser() == volunteer.getId()
    response.get(0).getInstitution() == institution.getId()
    response.get(0).getReview() == ASSESSMENT_VALID_REVIEW
  }

  def "get assessment with admin login"() {
    given:
    demoAdminLogin()

    when:
    def response = webClient.get()
      .uri('/assessment/' + institution.getId())
      .headers(httpHeaders -> httpHeaders.putAll(headers))
      .bodyValue(assessmentDto)
      .retrieve()
      .bodyToFlux(AssessmentDto.class)
      .collectList()
      .block()

    then: "check response"
    response != null
    response.get(0).getUser() == volunteer.getId()
    response.get(0).getInstitution() == institution.getId()
    response.get(0).getReview() == ASSESSMENT_VALID_REVIEW
  }
}
