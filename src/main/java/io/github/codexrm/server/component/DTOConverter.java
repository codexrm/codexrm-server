package io.github.codexrm.server.component;

import io.github.codexrm.server.dto.*;
import io.github.codexrm.server.model.*;
import io.github.codexrm.server.service.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DTOConverter {

    private final ModelMapper modelMapper;
    private final RoleService roleService;
    private final ValidateReference validation;

    @Autowired
    public DTOConverter(ModelMapper modelMapper, RoleService roleService) {
        this.modelMapper = modelMapper;
        this.roleService = roleService;
        this.validation = new ValidateReference();
    }

    // =========================
    // ====== REFERENCE ========
    // =========================

    public ReferenceDTO toReferenceDTO(final Reference reference) {

        if (reference instanceof ArticleReference) {
            return modelMapper.map(reference, ArticleReferenceDTO.class);

        } else if (reference instanceof BookSectionReference) {
            return modelMapper.map(reference, BookSectionReferenceDTO.class);

        } else if (reference instanceof BookReference) {
            return modelMapper.map(reference, BookReferenceDTO.class);

        } else if (reference instanceof BookLetReference) {
            return modelMapper.map(reference, BookLetReferenceDTO.class);

        } else if (reference instanceof ConferenceProceedingReference) {
            return modelMapper.map(reference, ConferenceProceedingsReferenceDTO.class);

        } else if (reference instanceof ConferencePaperReference) {
            return modelMapper.map(reference, ConferencePaperReferenceDTO.class);

        } else if (reference instanceof WebPageReference) {
            return modelMapper.map(reference, WebPageReferenceDTO.class);

        } else {
            return modelMapper.map(reference, ThesisReferenceDTO.class);
        }
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

    // =========================
    // ========= USER ==========
    // =========================

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