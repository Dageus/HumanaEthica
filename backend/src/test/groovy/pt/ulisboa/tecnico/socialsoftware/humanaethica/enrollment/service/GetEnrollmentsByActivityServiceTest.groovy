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
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@DataJpaTest
class GetEnrollmentsByActivityServiceTest extends SpockTest {
    private Activity activity

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

    def 'get two enrollments'() {
        when:
        def result = enrollmentService.getEnrollmentsByActivity(activity.id)

        then:
        result.size() == 2
        result.get(0).motivation == ENROLLMENT_MOTIVATION_1
        result.get(1).motivation == ENROLLMENT_MOTIVATION_2
    }

    def 'get enrollments with invalid activityId throws exception'() {
        when:
        enrollmentService.getEnrollmentsByActivity(-1)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_ID_INVALID
    }

    def 'get enrollments with null activityId throws exception'() {
        when:
        enrollmentService.getEnrollmentsByActivity(null)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_ID_NULL
    }

    def 'get enrollments with activity that does not exist'() {
        when:
        enrollmentService.getEnrollmentsByActivity(2)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ACTIVITY_NOT_FOUND
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
