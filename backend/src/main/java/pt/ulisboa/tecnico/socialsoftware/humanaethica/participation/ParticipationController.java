package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;

import java.util.List;


@RestController
@RequestMapping("/participations")
public class ParticipationController {
    @Autowired
    private ParticipationService participationService;

    @Value("${figures.dir}")
    private String figuresDir;

    @GetMapping()
    public List<ParticipationDto> getParticipations() {
        return participationService.getParticipations();
    }

    @GetMapping("/{activityId}")
    public List<ParticipationDto> getParticipationsByActivity(@PathVariable Integer activityId) {
        return participationService.getParticipationsByActivity(activityId);
    }

    @PostMapping("/{activityId}")
    @PreAuthorize("hasRole('ROLE_MEMBER') and hasPermission(#activityId, 'ACTIVITY.MEMBER')")
    public ParticipationDto createParticipation(@PathVariable Integer activityId, @RequestBody ParticipationDto participationDto) {
        return participationService.createParticipation(activityId, participationDto);
    }
}