package io.github.codexrm.server.domain.service;

import io.github.codexrm.server.component.ExportR;
import io.github.codexrm.server.component.ImportR;
import io.github.codexrm.server.domain.enums.SortReference;
import io.github.codexrm.server.infrastructure.exception.DuplicateResourceException;
import io.github.codexrm.server.infrastructure.exception.InvalidOperationException;
import io.github.codexrm.server.infrastructure.exception.ResourceNotFoundException;
import io.github.codexrm.server.domain.model.Reference;
import io.github.codexrm.server.domain.model.User;
import io.github.codexrm.server.infrastructure.persistence.repository.ReferenceRepository;
import org.jbibtex.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final ImportR importR;
    private final ExportR exportR;

    @Autowired
    public ReferenceService(final ReferenceRepository referenceRepository) {
        this.referenceRepository = referenceRepository;
        this.importR = new ImportR();
        this.exportR = new ExportR();
    }

    public Page<Reference> getAll(User user, String year, String title, int page, int size, SortReference sort) {

        Sort.Order order = getOrder(sort);
        Pageable pagingSort = PageRequest.of(page, size, Sort.by(order));

        if (year == null && title == null) {
            return referenceRepository.findByUser(user, pagingSort);

        } else if (year == null) {
            return referenceRepository.findByUserAndTitleContaining(user, title, pagingSort);

        } else if (title == null) {
            return referenceRepository.findByUserAndYearContaining(user, year, pagingSort);

        } else {
            return referenceRepository.findByUserAndYearContainingAndTitleContaining(user, year, title, pagingSort);
        }
    }

    public Reference get(Integer id) {
        return referenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reference", "id", id));
    }

    public Reference add(Reference reference) {

        if (reference.getId() != null && referenceRepository.existsById(reference.getId()))
            throw new DuplicateResourceException("Reference", "id", reference.getId());

        return referenceRepository.save(reference);
    }

    public Reference update(Reference reference) {

        if (reference.getId() == null)
            throw new ResourceNotFoundException("Reference", "id", null);

        get(reference.getId());

        return referenceRepository.save(reference);
    }

    public void delete(Integer id) {
        Reference reference = get(id);
        referenceRepository.delete(reference);
    }

    public void sync(User user, List<Reference> newReferenceList, List<Reference> updateReferenceList, List<Integer> deleteReferenceList) {

        if (newReferenceList != null) {
            for (Reference reference : newReferenceList) {
                add(reference);
            }
        }

        if (updateReferenceList != null) {
            for (Reference reference : updateReferenceList) {
                Reference existing = get(reference.getId());
                validateOwnership(user.getId(), existing);
                update(reference);
            }
        }

        if (deleteReferenceList != null) {
            for (Integer id : deleteReferenceList) {
                Reference existing = get(id);
                validateOwnership(user.getId(), existing);
                delete(id);
            }
        }
    }

    public ArrayList<Reference> importReferences(String path, String format) throws IOException, ParseException {
        return importR.importReferences(path, format);
    }

    public void exportReferences(File file, ArrayList<Reference> referenceList, String format) throws IOException {
        exportR.exportReferenceList(file, referenceList, format);
    }

    public Page<Reference> getAllFromUsers(String year, String title, int page, int size, SortReference sort) {

        Sort.Order order = getOrder(sort);
        Pageable pagingSort = PageRequest.of(page, size, Sort.by(order));

        if (year == null && title == null) {
            return referenceRepository.findAll(pagingSort);

        } else if (year == null) {
            return referenceRepository.findByTitleContaining(title, pagingSort);

        } else if (title == null) {
            return referenceRepository.findByYearContaining(year, pagingSort);

        } else {
            return referenceRepository.findByYearContainingAndTitleContaining(year, title, pagingSort);
        }
    }

    public void validateOwnership(Integer userId, Reference reference) {

        if (!reference.getUser().getId().equals(userId))
            throw new InvalidOperationException("You do not have permission to access this resource");
    }

    public ArrayList<Integer> filterOwnedReferences(Integer userId, ArrayList<Integer> referenceIds) {
        ArrayList<Integer> filtered = new ArrayList<>();

        for (Integer id : referenceIds) {
            Reference reference = get(id);
            try {
                validateOwnership(userId, reference);
                filtered.add(id);
            } catch (InvalidOperationException ignored) {
            }
        }
        return filtered;
    }

    private Sort.Order getOrder(SortReference sort) {

        if (sort == null)
            return new Sort.Order(Sort.Direction.ASC, "id");

        return switch (sort) {
            case idAsc -> Sort.Order.asc("id");
            case idDesc -> Sort.Order.desc("id");
            case yearAsc -> Sort.Order.asc("year");
            case yearDesc -> Sort.Order.desc("year");
            case titleAsc -> Sort.Order.asc("title");
            case titleDesc -> Sort.Order.desc("title");
            case monthAsc -> Sort.Order.asc("month");
            case monthDesc -> Sort.Order.desc("month");
            case noteAsc -> Sort.Order.asc("note");
            default -> Sort.Order.desc("note");
        };
    }
}