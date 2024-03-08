package pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.dto.EnrollmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

import pt.ulisboa.tecnico.socialsoftware.humanaethica.demo.DemoService
import pt.ulisboa.tecnico.socialsoftware.humanaethica.demo.DemoUtils

import spock.lang.Unroll
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class CreateEnrollmentMethodTest extends SpockTest {
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Enrollment otherEnrollment = Mock()

    def enrollmentDto

    def setup() {
        given: "enrollment info"
        enrollmentDto = new EnrollmentDto()
        enrollmentDto.setMotivation("This is a valid motivation with more than 10 characters")
        enrollmentDto.setEnrollmentDateTime(DateHandler.toISOString(NOW))
    }

    def "create enrollment successfully"() {
        given:
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        activity.getEnrollments() >> []
        enrollmentDto.setEnrollmentDateTime(DateHandler.toISOString(NOW))

        when:
        def enrollment = new Enrollment(activity, volunteer, enrollmentDto)

        then:
        enrollment.getMotivation() == "This is a valid motivation with more than 10 characters"
        enrollment.getEnrollmentDateTime() == NOW
    }

    def "create enrollment with invalid motivation"() {
        given:
        enrollmentDto.setMotivation("Too short")

        when:
        def enrollment = new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.MOTIVATION_TOO_SHORT
    }

    def "volunteer can only apply once to an activity"() {
        given: 
        volunteer.getId() >> 1
        activity.getApplicationDeadline() >> IN_TWO_DAYS
        otherEnrollment.getVolunteer() >> volunteer
        activity.getEnrollments() >> [otherEnrollment]

        when:
        def newEnrollment = new Enrollment(activity, volunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_ALREADY_ENROLLED
    }

    def "volunteer cannot apply after application period has ended"() {
        given:
        volunteer.getId() >> 1
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        otherEnrollment.getVolunteer() >> volunteer
        activity.getEnrollments() >> [otherEnrollment]
        enrollmentDto.setEnrollmentDateTime(DateHandler.toISOString(NOW))


        when:
        def enrollment = new Enrollment(activity, otherVolunteer, enrollmentDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.APPLICATION_PERIOD_CLOSED       
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}