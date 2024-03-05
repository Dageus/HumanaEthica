package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;

import java.security.Principal;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository.ParticipationRepository;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;

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

    @PostMapping()
    @PreAuthorize("(hasRole('ROLE_MEMBER'))")
    public ParticipationDto registerParticipation(Principal principal, @Valid @RequestBody ParticipationDto participationDto){
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return participationService.registerParticipation(userId, participationDto);
    }

    // TODO maybe needs more but i don't wanna half-ass this


    // ? private static final Logger logger = LoggerFactory.getLogger(ParticipationController.class);

    
}