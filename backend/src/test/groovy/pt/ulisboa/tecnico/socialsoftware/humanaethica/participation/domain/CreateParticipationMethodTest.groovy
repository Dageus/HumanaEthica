package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

import pt.ulisboa.tecnico.socialsoftware.humanaethica.demo.DemoService
import pt.ulisboa.tecnico.socialsoftware.humanaethica.demo.DemoUtils

import spock.lang.Unroll
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class CreateParticipationMethodTest extends SpockTest {
    Activity activity = Mock()
    Volunteer volunteer = Mock()
    Volunteer otherVolunteer = Mock()
    Participation otherParticipation = Mock()

    def participationDto 

    def setup() {
        given: "participation info"
        volunteer.getId() >> 1
        participationDto = new ParticipationDto()
        participationDto.setRating(5);
        participationDto.setAcceptanceDate(DateHandler.toISOString(NOW));
    }

    def "create participation with activity and volunteer"(){
        given:
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        activity.getParticipations() >> []
        activity.getParticipantsNumberLimit() >> 2
        volunteer.getParticipations().size() >> 0

        when:
        Participation participation = new Participation(activity, volunteer, participationDto)
        
        then: "check result"
        participation.getActivity() == activity
        participation.getVolunteer() == volunteer
        participation.getRating() == 5
        participation.getAcceptanceDate() == NOW
        
        // and: "invocations"
        // 1 * activity.addParticipation(_)
        // 1 * volunteer.addParticipation(_)
    }


    def "create participation and violate invariants number of participants is more than limit of activity"() {
        given:
        activity.getParticipantsNumberLimit() >> 1
        activity.getApplicationDeadline() >> ONE_DAY_AGO
        otherParticipation.getVolunteer() >> volunteer
        otherParticipation.getActivity() >> activity
        activity.getParticipations() >> [otherParticipation]
        volunteer.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, otherVolunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == errorMessage

        where:
        errorMessage = ErrorMessage.ACTIVITY_PARTICIPANTS_EXCEED_LIMIT
    }

    def "create participation and violate invariants participant can only be selected after enrollment period is over"() {
        given:
        activity.applicationDeadline >> IN_TWO_DAYS
        activity.getParticipantsNumberLimit() >> 2
        activity.getParticipations() >> []

        and: "a participation dto"
        participationDto = new ParticipationDto()
        participationDto.rating = 5;
        participationDto.acceptanceDate = DateHandler.toISOString(IN_ONE_DAY);

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == errorMessage

        where:
        errorMessage = ErrorMessage.ENROLLMENT_PROCESS_ONGOING
    }

    def "create participation and violate invariants volunteer can only participate once in an activity"() {
        given:
        volunteer.getId() >> 1
        activity.applicationDeadline >> ONE_DAY_AGO
        activity.getParticipantsNumberLimit() >> 2
        otherParticipation.getVolunteer() >> volunteer
        activity.getParticipations() >> [otherParticipation]

        when:
        new Participation(activity, volunteer, participationDto)

        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == errorMessage

        where:
        errorMessage = ErrorMessage.VOLUNTEER_ALREADY_PARTICIPATED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}