package io.github.codexrm.server.component;

import io.github.codexrm.server.dto.*;
import io.github.codexrm.server.model.*;
import io.github.codexrm.server.service.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DTOConverter {

    private final ModelMapper modelMapper;
    private final RoleService roleService;
    private final ValidateReference validation;

    /**
     * Registry pattern for mapping Reference entities to their corresponding DTOs.
     * This replaces the previous if-else chain to improve scalability and maintainability.
     * New mappings can be added without modifying the conversion logic.
     */
    private final Map<Class<?>, Class<? extends ReferenceDTO>> referenceDTOMap = new HashMap<>();

    @Autowired
    public DTOConverter(ModelMapper modelMapper, RoleService roleService) {
        this.modelMapper = modelMapper;
        this.roleService = roleService;
        this.validation = new ValidateReference();

        initReferenceMap();
    }

    private void initReferenceMap() {

        referenceDTOMap.put(ArticleReference.class, ArticleReferenceDTO.class);
        referenceDTOMap.put(BookSectionReference.class, BookSectionReferenceDTO.class);
        referenceDTOMap.put(BookReference.class, BookReferenceDTO.class);
        referenceDTOMap.put(BookLetReference.class, BookLetReferenceDTO.class);
        referenceDTOMap.put(ConferenceProceedingReference.class, ConferenceProceedingsReferenceDTO.class);
        referenceDTOMap.put(ConferencePaperReference.class, ConferencePaperReferenceDTO.class);
        referenceDTOMap.put(WebPageReference.class, WebPageReferenceDTO.class);
        referenceDTOMap.put(ThesisReference.class, ThesisReferenceDTO.class);
    }


    // REFERENCE
    public ReferenceDTO toReferenceDTO(final Reference reference) {

        Class<? extends ReferenceDTO> dtoClass =
                referenceDTOMap.get(reference.getClass());

        if (dtoClass == null) {
            throw new IllegalArgumentException(
                    "No DTO mapping found for " + reference.getClass().getSimpleName()
            );
        }

        return modelMapper.map(reference, dtoClass);
    }

    public Reference toReference(final ReferenceDTO referenceDTO, User user) {

        Reference reference;

        if (referenceDTO instanceof ArticleReferenceDTO dto) {

            ArticleReference entity = modelMapper.map(dto, ArticleReference.class);
            validation.validateArticleReference(entity);
            reference = validation.validateRequiredArticle(entity);

        } else if (referenceDTO instanceof BookSectionReferenceDTO dto) {

            BookSectionReference entity = modelMapper.map(dto, BookSectionReference.class);
            validation.validateBookSectionReference(entity);
            reference = validation.validateRequiredBookSection(entity);

        } else if (referenceDTO instanceof BookReferenceDTO dto) {

            BookReference entity = modelMapper.map(dto, BookReference.class);
            validation.validateBookReference(entity);
            reference = validation.validateRequiredBook(entity);

        } else if (referenceDTO instanceof BookLetReferenceDTO dto) {

            BookLetReference entity = modelMapper.map(dto, BookLetReference.class);
            validation.validateBookLetReference(entity);
            reference = validation.validateRequiredBookLet(entity);

        } else if (referenceDTO instanceof ConferenceProceedingsReferenceDTO dto) {

            ConferenceProceedingReference entity = modelMapper.map(dto, ConferenceProceedingReference.class);
            validation.validateConferenceProceedingsReference(entity);
            reference = validation.validateRequiredConferenceProceedings(entity);

        } else if (referenceDTO instanceof ConferencePaperReferenceDTO dto) {

            ConferencePaperReference entity = modelMapper.map(dto, ConferencePaperReference.class);
            validation.validateConferencePaperReference(entity);
            reference = validation.validateRequiredConferencePaper(entity);

        } else if (referenceDTO instanceof WebPageReferenceDTO dto) {

            WebPageReference entity = modelMapper.map(dto, WebPageReference.class);
            validation.validateWebPageReference(entity);
            reference = entity;

        } else {

            ThesisReference entity = modelMapper.map(referenceDTO, ThesisReference.class);
            validation.validateThesisReference(entity);
            reference = validation.validateRequiredThesis(entity);
        }

        reference.setUser(user);
        return reference;
    }

    public List<ReferenceDTO> toReferenceDTOList(final List<Reference> referenceList) {
        return referenceList.stream()
                .map(this::toReferenceDTO)
                .toList();
    }

    public List<Reference> toReferenceList(final List<ReferenceDTO> referenceDTOList, User user) {

        List<Reference> referenceList = new ArrayList<>();

        for (ReferenceDTO dto : referenceDTOList) {
            Reference reference = toReference(dto, user);
            if (reference != null) {
                referenceList.add(reference);
            }
        }
        return referenceList;
    }

    public Reference createReference(final ReferenceDTO referenceDTO, User user) {
        return toReference(referenceDTO, user);
    }

    //  USER
    public List<UserDTO> toUserDTOList(final List<User> userList) {
        return userList.stream()
                .map(this::toUserDTO)
                .toList();
    }

    public UserDTO toUserDTO(final User user) {

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.getRoles().clear();

        user.getRoles().forEach(role ->
                userDTO.setRol(role.getName().name())
        );

        return userDTO;
    }

    public User toUser(final UserDTO userDTO) {

        User user = modelMapper.map(userDTO, User.class);
        user.getRoles().clear();
        user.setRoles(roleService.resolveRoles(userDTO.getRoles()));
        return user;
    }
}