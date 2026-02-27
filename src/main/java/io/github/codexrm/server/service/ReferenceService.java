package io.github.codexrm.server.service;

import io.github.codexrm.server.component.ExportR;
import io.github.codexrm.server.component.ImportR;
import io.github.codexrm.server.enums.SortReference;
import io.github.codexrm.server.model.Reference;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.repository.ReferenceRepository;
import org.jbibtex.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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
       
        } else {
            if (year == null) {
                return referenceRepository.findByUserAndTitleContaining(user, title, pagingSort);
           
            } else {
                if (title == null) {
                    return referenceRepository.findByUserAndYearContaining(user, year, pagingSort);
              
                } else {
                    return null;
                }
            }
        }
    }

    public Reference get(Integer id) {
        return referenceRepository.findById(id).get();
    }

    public Reference add(Reference reference) {
        if (reference.getId() != null && referenceRepository.existsById(reference.getId()))
            throw new EntityExistsException("There is already existing entity with such ID in the database.");

        return referenceRepository.save(reference);
    }

    public Reference update(Reference reference) {
        if (reference.getId() != null && !referenceRepository.existsById(reference.getId()))
            throw new EntityNotFoundException("There is no entity with such ID in the database.");

        return referenceRepository.save(reference);
    }

    public void delete(Integer id) {
        referenceRepository.deleteById(id);
    }

    public void sync(List<Reference> newReferenceList, List<Reference> updateReferenceList, List<Integer> deleteReferenceList) {
        for (Reference reference : newReferenceList) {
            add(reference);
        }
        for (Reference reference : updateReferenceList) {
            update(reference);
        }
        for (Integer id : deleteReferenceList) {
            delete(id);
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
        
        } else {
            if (year == null) {
                return referenceRepository.findByTitleContaining(title, pagingSort);
           
            } else {
                if (title == null) {
                    return referenceRepository.findByYearContaining(year, pagingSort);
               
                } else {
                    return null;
                }
            }
        }
    }

    private Sort.Order getOrder(SortReference sort) {
        if (sort == null)
            return new Sort.Order(Sort.Direction.ASC, "id");

         else {
            switch (sort) {
                case idAsc:
                    return new Sort.Order(Sort.Direction.ASC, "id");

                case idDesc:
                    return new Sort.Order(Sort.Direction.DESC, "id");

                case yearAsc:
                    return new Sort.Order(Sort.Direction.ASC, "year");

                case yearDesc:
                    return new Sort.Order(Sort.Direction.DESC, "year");

                case titleAsc:
                    return new Sort.Order(Sort.Direction.ASC, "title");

                case titleDesc:
                    return new Sort.Order(Sort.Direction.DESC, "title");

                case monthAsc:
                    return new Sort.Order(Sort.Direction.ASC, "month");

                case monthDesc:
                    return new Sort.Order(Sort.Direction.DESC, "month");

                case noteAsc:
                    return new Sort.Order(Sort.Direction.ASC, "note");

                default:
                    return new Sort.Order(Sort.Direction.DESC, "note");
            }
        }
    }
}
