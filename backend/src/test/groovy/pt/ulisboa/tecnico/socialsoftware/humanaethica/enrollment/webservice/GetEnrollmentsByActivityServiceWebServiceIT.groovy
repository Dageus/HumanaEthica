package pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.webservice

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
class GetEnrollmentsByActivityServiceWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    private Activity activity

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
        def volunteer_1 = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)  
        and: "another user info"
        def volunteer_2 = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.ACTIVE)
        and: "enrollment info"
        def enrollmentDto_1 = createEnrollmentDto(ENROLLMENT_MOTIVATION_1, NOW, null, null)
        and: "another enrollment info"
        def enrollmentDto_2 = createEnrollmentDto(ENROLLMENT_MOTIVATION_2, NOW, null, null)
        and: "an enrollment"
        def enrollment_1 = new Enrollment(activity, volunteer_1, enrollmentDto_1)
        enrollmentRepository.save(enrollment_1)
        and: "another enrollment"
        def enrollment_2 = new Enrollment(activity, volunteer_2, enrollmentDto_2)
        enrollmentRepository.save(enrollment_2)
    }

    def "get enrollments as a member by activity"() {
        given: 'a member'
        demoMemberLogin()
        when:
        def response = webClient.get()
                .uri('/enrollments/activities/' + activity.id + '/enrollments')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "check response"
        response.size() == 2
        response.get(1).motivation == ENROLLMENT_MOTIVATION_2
        response.get(1).volunteer.name == USER_2_NAME
        response.get(1).activity.name == ACTIVITY_NAME_1

        cleanup:
        deleteAll()
    }

    def "get enrollments as a volunteer by activity"() {
        given: 'a volunteer'
        demoVolunteerLogin()
        when:
        def response = webClient.get()
                .uri('/enrollments/activities/' + activity.id + '/enrollments')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(EnrollmentDto.class)
                .collectList()
                .block()

        then: "exception is thrown"
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }
}
