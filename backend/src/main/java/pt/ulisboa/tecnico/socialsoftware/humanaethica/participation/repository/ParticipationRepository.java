package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;

import java.util.List;


@Repository
@Transactional
public interface ParticipationRepository extends JpaRepository<Participation, Integer> {
    @Query(value = "select * from participations u where u.activity_id = lower(:activityId)", nativeQuery = true)
    Optional<List<Participation>> findParticipationsByActivity(Integer activityId);
}