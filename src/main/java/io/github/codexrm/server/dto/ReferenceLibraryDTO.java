package io.github.codexrm.server.dto;

import io.github.codexrm.server.enums.SortReference;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Reference library synchronization payload containing new, updated, and deleted references")
public class ReferenceLibraryDTO {

    @Schema(
            description = "List of new references to be created",
            example = "[{ \"referenceType\": \"ArticleReferenceDTO\", \"title\": \"Deep Learning\", \"year\": \"2016\" }]")
    private List<ReferenceDTO> newReferencesList;

    @Schema(
            description = "List of references to be updated",
            example = "[{ \"id\": 1, \"referenceType\": \"BookReferenceDTO\", \"title\": \"Clean Code\", \"year\": \"2008\" }]")
    private List<ReferenceDTO> updatedReferencesList;

    @Schema(
            description = "List of reference IDs that should be deleted",
            example = "[1, 2, 3]")
    private List<Integer> deletedReferencesList;

    @Schema( description = "Sorting configuration applied after synchronization")
    private SortReference sortReference;

    public ReferenceLibraryDTO() {
        this.newReferencesList = new ArrayList<>();
        this.updatedReferencesList = new ArrayList<>();
        this.deletedReferencesList = new ArrayList<>();
    }

    public ReferenceLibraryDTO(List<ReferenceDTO> newReferencesList, List<ReferenceDTO> updatedReferencesList, List<Integer> deletedReferencesList, SortReference sortReference) {
        this.newReferencesList = newReferencesList;
        this.updatedReferencesList = updatedReferencesList;
        this.deletedReferencesList = deletedReferencesList;
        this.sortReference = sortReference;
    }

    public List<ReferenceDTO> getNewReferencesList() {
        return newReferencesList;
    }

    public void setNewReferencesList(List<ReferenceDTO> newReferencesList) {
        this.newReferencesList = newReferencesList;
    }

    public List<ReferenceDTO> getUpdatedReferencesList() {
        return updatedReferencesList;
    }

    public void setUpdatedReferencesList(List<ReferenceDTO> updatedReferencesList) {
        this.updatedReferencesList = updatedReferencesList;
    }

    public List<Integer> getDeletedReferencesList() {
        return deletedReferencesList;
    }

    public void setDeletedReferencesList(List<Integer> deletedReferencesList) {
        this.deletedReferencesList = deletedReferencesList;
    }

    public SortReference getSortReference() {
        return sortReference;
    }

    public void setSortReference(SortReference sortReference) {
        this.sortReference = sortReference;
    }
}