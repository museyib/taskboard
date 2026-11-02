package llc.devlab.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import llc.devlab.app.model.dto.LabelCreateRequest;
import llc.devlab.app.model.dto.LabelDto;
import llc.devlab.app.model.dto.LabelUpdateRequest;
import llc.devlab.app.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {
    private final LabelService labelService;

    @Operation(
            description = "Get labels",
            summary = "Get labels"
    )    @GetMapping
    public ResponseEntity<List<LabelDto>> getLabels() {
        return ResponseEntity.ok(labelService.getLabels());
    }


    @Operation(
            description = "Get label by id",
            summary = "Get label by id"
    )    @GetMapping("/{id}")
    public ResponseEntity<LabelDto> getLabelById(@PathVariable Long id) {
        return ResponseEntity.ok(labelService.getLabelById(id));
    }


    @Operation(
            description = "Create label",
            summary = "Create label"
    )    @PostMapping
    public ResponseEntity<LabelDto> createLabel(@Valid @RequestBody LabelCreateRequest request) {
        return ResponseEntity.ok(labelService.createLabel(request));
    }


    @Operation(
            description = "Update label",
            summary = "Update label"
    )    @PutMapping("/{id}")
    public ResponseEntity<LabelDto> updateLabel(@PathVariable Long id,
                                                 @RequestBody LabelUpdateRequest request) {
        return ResponseEntity.ok(labelService.updateLabel(id, request));
    }


    @Operation(
            description = "Delete label",
            summary = "Delete label"
    )    @DeleteMapping("/{id}")
    public void deleteLabel(@PathVariable Long id) {
        labelService.deleteLabel(id);
    }
}
