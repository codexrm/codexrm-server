package io.github.codexrm.server.dto;

import io.github.codexrm.server.enums.SortReference;

import java.util.ArrayList;
import java.util.List;

public class ReferenceLibraryDTO {

    private List<ReferenceDTO> newReferencesList;
    private List<ReferenceDTO> updatedReferencesList;
    private List<Integer> deletedReferencesList;
    private Integer userId;
    private SortReference sortReference;

    public ReferenceLibraryDTO() {
        this.newReferencesList = new ArrayList<>();
        this.updatedReferencesList = new ArrayList<>();
        this.deletedReferencesList = new ArrayList<>();
    }

    public ReferenceLibraryDTO(List<ReferenceDTO> newReferencesList, List<ReferenceDTO> updatedReferencesList, List<Integer> deletedReferencesList, Integer userId, SortReference sortReference) {
        this.newReferencesList = newReferencesList;
        this.updatedReferencesList = updatedReferencesList;
        this.deletedReferencesList = deletedReferencesList;
        this.userId = userId;
        this.sortReference = sortReference;
    }

    public List<ReferenceDTO> getNewReferencesList() { return newReferencesList; }

    public void setNewReferencesList(List<ReferenceDTO> newReferencesList) { this.newReferencesList = newReferencesList; }

    public List<ReferenceDTO> getUpdatedReferencesList() { return updatedReferencesList; }

    public void setUpdatedReferencesList(List<ReferenceDTO> updatedReferencesList) { this.updatedReferencesList = updatedReferencesList; }

    public List<Integer> getDeletedReferencesList() { return deletedReferencesList; }

    public void setDeletedReferencesList(List<Integer> deletedReferencesList) { this.deletedReferencesList = deletedReferencesList; }

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public SortReference getSortReference() { return sortReference; }

    public void setSortReference(SortReference sortReference) { this.sortReference = sortReference; }
}