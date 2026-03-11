package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response containing a list of references")
public class ReferencePageDTO {

    @Schema(description = "List of references returned for the current page")
    private List<ReferenceDTO> referenceDTOList;

    @Schema(description = "Pagination metadata associated with the reference list")
    private PageDTO pageDTO;

    public ReferencePageDTO() {}

    public ReferencePageDTO(List<ReferenceDTO> referenceDTOList, PageDTO pageDTO) {
        this.referenceDTOList = referenceDTOList;
        this.pageDTO = pageDTO;
    }

    public List<ReferenceDTO> getReferenceDTOList() {
        return referenceDTOList;
    }

    public void setReferenceDTOList(List<ReferenceDTO> referenceDTOList) {
        this.referenceDTOList = referenceDTOList;
    }

    public PageDTO getPageDTO() {
        return pageDTO;
    }

    public void setPageDTO(PageDTO pageDTO) {
        this.pageDTO = pageDTO;
    }
}
