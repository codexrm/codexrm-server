package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "Paginated response containing a list of references")
public class ReferencePageDTO {

    @Schema(description = "List of references returned for the current page")
    private List<ReferenceDTO> referenceDTOList;

    @Schema(description = "Pagination metadata associated with the reference list")
    @NotBlank
    private PageDTO pageDTO;

    public ReferencePageDTO() {}

    public ReferencePageDTO(List<ReferenceDTO> referenceDTOList, PageDTO pageDTO) {
        this.referenceDTOList = referenceDTOList;
        this.pageDTO = pageDTO;
    }
}
