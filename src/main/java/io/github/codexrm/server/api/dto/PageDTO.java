package io.github.codexrm.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Pagination information for paged responses")
public class PageDTO {

    @Schema(description = "Current page number", example = "0")
    @NotBlank
    private Integer currentPage;

    @Schema(description = "Total number of elements available", example = "125")
    @NotBlank
    private Long totalElement;

    @Schema(description = "Total number of pages available", example = "13")
    @NotBlank
    private Integer totalPages;

    public PageDTO() {}

    public PageDTO(Integer currentPage, Long totalElement, Integer totalPages) {
        this.currentPage = currentPage;
        this.totalElement = totalElement;
        this.totalPages = totalPages;
    }
}
