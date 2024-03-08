package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateEnrollmentServiceWebServiceIT extends SpockTest {
  @LocalServerPort
  private int port
  private Activity activity
  private EnrollmentDto enrollmentDto

  def setup() {
    deleteAll()

    webClient = WebClient.create("http://localhost:" + port)
    headers = new HttpHeaders()
    headers.setContentType(MediaType.APPLICATION_JSON)

    def institution = institutionService.getDemoInstitution()
    given: "activity info"
    def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,1,ACTIVITY_DESCRIPTION_1,
            IN_ONE_DAY,IN_TWO_DAYS,IN_THREE_DAYS,null)
    and: "a theme"
    def themes = new ArrayList<>()
    themes.add(createTheme(THEME_NAME_1, Theme.State.APPROVED,null))
    and: "an activity"
    activity = new Activity(activityDto, institution, themes)
    activity = activityRepository.save(activity)
    and: "user info"
    def volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)  
    and: "enrollment info"
    enrollmentDto = createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null)
    and: "an enrollment"
    def enrollment = new Enrollment(activity, volunteer, enrollmentDto)
    enrollment = enrollmentRepository.save(enrollment)
  }

  def "login as volunteer, and create a enrollment"() {
    given: 'a volunteer'
    def loggedUser = demoVolunteerLogin()

    when:
    def response = webClient.post()
            .uri('/enrollments/activities/' + activity.id + '/apply')
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .bodyValue(enrollmentDto)
            .retrieve()
            .bodyToMono(EnrollmentDto.class)
            .block()

    then: "check database"
    response.motivation == ENROLLMENT_MOTIVATION_1
    response.volunteer.name == loggedUser.getName()
    response.activity.name == ACTIVITY_NAME_1

    cleanup:
    deleteAll()
  }

  def "login as admin, and create a enrollment"() {
    given: 'admin login'
    demoAdminLogin()

    when:
    webClient.post()
            .uri('/enrollments/activities/' + activity.id + '/apply')
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .bodyValue(enrollmentDto)
            .retrieve()
            .toBodilessEntity()
            .block()

    then: "exception is thrown"
    def error = thrown(WebClientResponseException)
    error.statusCode == HttpStatus.FORBIDDEN

    cleanup:
    deleteAll()
  }

  def "login as member, and create a enrollment"() {
    given: 'member login'
    demoMemberLogin()

    when:
    webClient.post()
            .uri('enrollments/activities/' + activity.id + '/apply')
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .bodyValue(enrollmentDto)
            .retrieve()
            .toBodilessEntity()
            .block()
    
    then: "exception is thrown"
    def error = thrown(WebClientResponseException)
    error.statusCode == HttpStatus.FORBIDDEN

    cleanup:
    deleteAll()
  }
}
