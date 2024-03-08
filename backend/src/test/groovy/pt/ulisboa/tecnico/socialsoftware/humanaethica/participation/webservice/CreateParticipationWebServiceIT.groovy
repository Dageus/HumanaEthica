package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.webservice

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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateParticipationServiceWebServiceIT extends SpockTest {
  @LocalServerPort
  private int port
  private Activity activity
  private ParticipationDto participationDto

  def setup() {
    deleteAll()

    webClient = WebClient.create("http://localhost:" + port)
    headers = new HttpHeaders()
    headers.setContentType(MediaType.APPLICATION_JSON)

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
    def volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)  
    and: "participation info"
    participationDto = createParticipationDto(RATING_1, NOW, null, null)
    and: "an participation"
    def participation = new Participation(activity, volunteer, participationDto)
    participation = participationRepository.save(participation)
  }

  // def "login as volunteer, and create a participation"() {
  //   given: 'a volunteer'
  //   def loggedUser = demoVolunteerLogin()

  //   when:
  //   def response = webClient.post()
  //           .uri('/participations/' + activity.id)
  //           .headers(httpHeaders -> httpHeaders.putAll(headers))
  //           .bodyValue(participationDto)
  //           .retrieve()
  //           .bodyToMono(ParticipationDto.class)
  //           .block()

  //   then: "check database"
  //   response.rating == RATING_1
  //   response.volunteer.name == loggedUser.getName()
  //   response.activity.name == ACTIVITY_NAME_1

  //   cleanup:
  //   deleteAll()
  // }

  def "login as admin, and create a participation"() {
    given: 'admin login'
    demoAdminLogin()

    when:
    webClient.post()
            .uri('/participations/' + activity.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .bodyValue(participationDto)
            .retrieve()
            .toBodilessEntity()
            .block()

    then: "exception is thrown"
    def error = thrown(WebClientResponseException)
    error.statusCode == HttpStatus.FORBIDDEN

    cleanup:
    deleteAll()
  }

  // def "login as member, and create a participation"() {
  //   given: 'member login'
  //   demoMemberLogin()

  //   when:
  //   webClient.post()
  //           .uri('/participations/' + activity.id)
  //           .headers(httpHeaders -> httpHeaders.putAll(headers))
  //           .bodyValue(participationDto)
  //           .retrieve()
  //           .toBodilessEntity()
  //           .block()
    
  //   then: "exception is thrown"
  //   def error = thrown(WebClientResponseException)
  //   error.statusCode == HttpStatus.FORBIDDEN

  //   cleanup:
  //   deleteAll()
  // }
}
