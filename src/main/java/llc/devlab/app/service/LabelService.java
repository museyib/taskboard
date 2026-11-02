package llc.devlab.app.service;

import jakarta.transaction.Transactional;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.mapper.LabelMapper;
import llc.devlab.app.model.dto.LabelCreateRequest;
import llc.devlab.app.model.dto.LabelDto;
import llc.devlab.app.model.dto.LabelUpdateRequest;
import llc.devlab.app.model.entity.Label;
import llc.devlab.app.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    public List<LabelDto> getLabels() {
        return labelMapper.toDtoList(labelRepository.findAllByDeletedFalse());
    }

    public LabelDto getLabelById(Long id) {
        return labelMapper.toDto(labelRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found")));
    }

    @Transactional
    public LabelDto createLabel(LabelCreateRequest request) {
        return labelMapper.toDto(labelRepository.save(labelMapper.toEntity(request)));
    }

    @Transactional
    public LabelDto updateLabel(Long id, LabelUpdateRequest request) {
        Label label = labelRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        labelMapper.updateEntity(request, label);
        return labelMapper.toDto(labelRepository.save(label));
    }

    @Transactional
    public void deleteLabel(Long id) {
        Label label = labelRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        label.setDeleted(true);
        labelRepository.save(label);
    }
}
