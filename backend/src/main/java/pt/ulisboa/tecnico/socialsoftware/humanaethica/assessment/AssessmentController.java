package pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;

import org.springframework.security.core.Authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentController.class);

    @Autowired
    private AssessmentService assessmentService;

    @GetMapping("/{institutionId}")
    public List<AssessmentDto> getInstitutionAssessments(@PathVariable Integer institutionId) {
        return assessmentService.getAssessmentsByInstitution(institutionId);
    }

    @PostMapping("/{institutionId}")
    @PreAuthorize("hasRole('ROLE_VOLUNTEER')")
    public AssessmentDto createAssessment(Principal principal, @PathVariable Integer institutionId,
            @Valid @RequestBody AssessmentDto assessmentDto) {
        Integer userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();

        logger.info("User " + userId + " is registering an assessment.");

        return assessmentService.createAssessment(userId, institutionId, assessmentDto);
    }

}
