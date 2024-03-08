package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.repository.ActivityRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository.ParticipationRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class ParticipationService {
    @Autowired
    ParticipationRepository participationRepository;
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    UserRepository userRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ParticipationDto> getParticipations() {
        return participationRepository.findAll().stream()
                .map(participation -> new ParticipationDto(participation, true, true))
                .sorted(Comparator.comparing(ParticipationDto::getAcceptanceDate))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<ParticipationDto> getParticipationsByActivity(Integer activityId) {
        if (activityId == null)
            throw new HEException(ACTIVITY_ID_NULL);
        if (activityId <= 0)
            throw new HEException(ACTIVITY_ID_INVALID, activityId);

        activityRepository.findById(activityId)
            .orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        List<Participation> participations = participationRepository.getParticipationsByActivity(activityId);

        List<ParticipationDto> participationDtos = participations.stream()
            .map(participation -> new ParticipationDto(participation, true, true))
            .sorted(Comparator.comparing(ParticipationDto::getAcceptanceDate))
            .toList();

        return participationDtos;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ParticipationDto createParticipation(Integer activityId, ParticipationDto participationDto) {
        if (activityId == null)
            throw new HEException(ACTIVITY_NOT_FOUND);
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new HEException(ACTIVITY_NOT_FOUND, activityId));

        if (participationDto.getVolunteer() == null)
            throw new HEException(VOLUNTEER_DOES_NOT_EXIST);

        Volunteer volunteer = (Volunteer) userRepository.findById(participationDto.getVolunteer().getId())
        .orElseThrow(() -> new HEException(VOLUNTEER_DOES_NOT_EXIST));


        Participation participation = new Participation(activity, volunteer, participationDto);

        participationRepository.save(participation);

        return new ParticipationDto(participation, true, true);
    }

}