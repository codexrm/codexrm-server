package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

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

    public Integer getCurrentPage() { return currentPage; }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Long getTotalElement() {
        return totalElement;
    }

    public void setTotalElement(Long totalElement) {
        this.totalElement = totalElement;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
