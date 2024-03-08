package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.INSTITUTIONID_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.INSTITUTIONID_NOT_VALID;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.USERID_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.USERID_NOT_VALID;

import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionRepository institutionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AssessmentDto createAssessment(Integer userId, Integer institutionId,
            AssessmentDto assessmentDto) {
        if (userId == null)
            throw new HEException(USERID_NOT_VALID, -1);
        if (userId <= 0)
            throw new HEException(USERID_NOT_VALID, userId);
        if (institutionId == null)
            throw new HEException(INSTITUTIONID_NOT_VALID, -1);
        if (institutionId <= 0)
            throw new HEException(INSTITUTIONID_NOT_VALID, institutionId);

        Volunteer volunteer = (Volunteer) userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USERID_NOT_FOUND, userId));

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTIONID_NOT_FOUND, institutionId));

        Assessment assessment = new Assessment(institution, volunteer, assessmentDto);

        assessmentRepository.save(assessment);

        return new AssessmentDto(assessment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<AssessmentDto> getAssessmentsByInstitution(Integer institutionId) {
        if (institutionId == null)
            throw new HEException(INSTITUTIONID_NOT_VALID, -1);
        if (institutionId <= 0)
            throw new HEException(INSTITUTIONID_NOT_VALID, institutionId);

        boolean exists = institutionRepository.existsById(institutionId);

        if (!exists)
            throw new HEException(INSTITUTIONID_NOT_FOUND, institutionId);

        return assessmentRepository.findByInstitutionId(institutionId).stream()
                .map(assessment -> new AssessmentDto(assessment))
                .toList();
    }

}
