package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.demo.DemoUtils;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Document;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.DocumentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.UserApplicationalService;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.UserService;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.UserDocument;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User.State;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.RegisterUserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.LinkHandler;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.Mailer;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstitutionService {
    
    @Autowired
    InstitutionRepository institutionRepository;
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentRepository documentRepository;
    
    @Autowired
    private UserApplicationalService userApplicationalService;

    @Autowired
    private Mailer mailer;

    public static final String PASSWORD_CONFIRMATION_MAIL_SUBJECT = "Password Confirmation";

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<InstitutionDto> getInstitutions() {
        return institutionRepository.findAll().stream()
                .map(InstitutionDto::new)
                .sorted(Comparator.comparing(InstitutionDto::getName))
                .collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Document getInstitutionDocument(Integer id) {
        return (institutionRepository.findById(id).orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND))).getDocument();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteInstitution(int institutionId) {
        Institution institution = institutionRepository.findById(institutionId).orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND));

        institution.delete();

        for (User user : institution.getMembers()){
            user.remove();
        }
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void registerInstitutionMemberPair(InstitutionDto institutionDto, Document document, RegisterUserDto registerUserDto, UserDocument memberDocument){
        Institution i = registerInstitution(institutionDto);
        addDocument(i, document);

        registerUserDto.setInstitutionId(i.getId());
        UserDto userDto = userApplicationalService.registerUser(registerUserDto);
        userApplicationalService.addDocument(userDto, memberDocument);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Institution registerInstitution(InstitutionDto institutionDto) {

        if (institutionDto.getName() == null || institutionDto.getName().trim().length() == 0) {
            throw new HEException(INVALID_INSTITUTION_NAME, institutionDto.getName());
        }

        if (institutionDto.getEmail() == null || !institutionDto.getEmail().matches(UserService.MAIL_FORMAT)) {
            throw new HEException(INVALID_EMAIL, institutionDto.getEmail());
        }

        if (institutionDto.getNif() == null || institutionDto.getNif().length() != 9) {
            throw new HEException(INVALID_NIF, institutionDto.getEmail());
        }

        try {
            Integer.parseInt(institutionDto.getNif());
        } catch (NumberFormatException nfe) {
            throw new HEException(INVALID_NIF, institutionDto.getEmail());
        }

        if (institutionRepository.findInstitutionByNif(institutionDto.getNif()).isPresent()) {
            throw new HEException(NIF_ALREADY_EXIST, institutionDto.getNif());
        }

        Institution institution = new Institution(institutionDto.getName(), institutionDto.getEmail(), institutionDto.getNif());

        institutionRepository.save(institution);

        return institution;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addDocument(Institution institution, Document doc) {
        institution.setDocument(doc);
        documentRepository.save(doc);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void validateInstitution(int id){
        Institution institution = institutionRepository.findById(id).orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND));
        Member member = institution.getMembers().isEmpty() ? null : institution.getMembers().get(0);
        if (member != null && member.getState().equals(State.SUBMITTED))
            throw new HEException(USER_NOT_APPROVED);
        if (member != null && !institution.isActive())
            sendConfirmationEmailTo(member.getUsername(), member.getEmail(), institution.generateConfirmationToken());
        institution.validate();
    }

    public void sendConfirmationEmailTo(String username, String email, String token) {
        mailer.sendSimpleMail(mailUsername, email, Mailer.QUIZZES_TUTOR_SUBJECT + PASSWORD_CONFIRMATION_MAIL_SUBJECT, buildMailBody(username, token));
    }

    private String buildMailBody(String username, String token) {
        String msg = "To confirm your registration, as external user using username (" + username + ") click the following link";
        return String.format("%s: %s", msg, LinkHandler.createConfirmRegistrationLink(username, token));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Institution getDemoInstitution() {
        return institutionRepository.findInstitutionByNif(DemoUtils.DEMO_INSTITUTION).orElseGet(() -> {
            Institution institution = new Institution(DemoUtils.DEMO_INSTITUTION, "demo_institution@mail.com", DemoUtils.DEMO_INSTITUTION_NIF);
            institutionRepository.save(institution);
            return institution;
        });
    }
}
