package io.github.codexrm.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Paginated response containing a list of users")
public class UserPageDTO {

    @Schema(description = "List of users returned for the current page")
    private List<UserDTO> userDTOList;

    @Schema(description = "Pagination metadata associated with the user list")
    @NotBlank
    private PageDTO pageDTO;

    public UserPageDTO() {
        userDTOList = new ArrayList<>();
    }

    public UserPageDTO(List<UserDTO> userList, PageDTO pageDTO) {
        this.userDTOList = userList;
        this.pageDTO = pageDTO;
    }

    public List<UserDTO> getUserDTOList() {
        return userDTOList;
    }

    public void setUserDTOList(List<UserDTO> userDTOList) {
        this.userDTOList = userDTOList;
    }

    public PageDTO getPageDTO() {
        return pageDTO;
    }

    public void setPageDTO(PageDTO pageDTO) {
        this.pageDTO = pageDTO;
    }
}
