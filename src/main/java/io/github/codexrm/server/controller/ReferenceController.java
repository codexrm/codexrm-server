package io.github.codexrm.server.controller;

import io.github.codexrm.server.component.DTOConverter;
import io.github.codexrm.server.dto.UserDTO;
import io.github.codexrm.server.model.Reference;
import io.github.codexrm.server.model.User;
import io.github.codexrm.server.dto.ReferenceDTO;
import io.github.codexrm.server.service.ReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/Reference")
@RestController
public class ReferenceController {

    private final ReferenceService referenceService;
    private final DTOConverter dtoConverter;

    @Autowired
    public ReferenceController(ReferenceService referenceService, DTOConverter dtoConverter) {
        this.referenceService = referenceService;
        this.dtoConverter = dtoConverter;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ReferenceDTO>> getAll(@RequestBody final UserDTO userDTO){
        User user = dtoConverter.toUser(userDTO);
        List<ReferenceDTO> referenceDTOList = dtoConverter.toReferenceDTOList(referenceService.findAll(user));
        return ResponseEntity.ok().body(referenceDTOList);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ReferenceDTO> getById(@PathVariable final Integer id){
        ReferenceDTO referenceDTO = dtoConverter.toReferenceDTO(referenceService.findById(id));
        return ResponseEntity.ok().body(referenceDTO);
    }

    @PostMapping("/add")
    public ResponseEntity<ReferenceDTO> add(@RequestBody final ReferenceDTO referenceDTO){
        Reference reference = dtoConverter.toReference(referenceDTO);
        ReferenceDTO referenceDTOAdded = dtoConverter.toReferenceDTO(referenceService.save(reference));
        return new ResponseEntity<>(referenceDTOAdded, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<ReferenceDTO> update( @RequestBody final ReferenceDTO referenceDTO){
        Reference reference = dtoConverter.toReference(referenceDTO);
        ReferenceDTO referenceDTOUpdated = dtoConverter.toReferenceDTO(referenceService.update(reference));
        return new ResponseEntity<>(referenceDTOUpdated, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable final Integer id) {
        referenceService.delete(id);
        return ResponseEntity.ok().build();
    }
}
