package llc.devlab.app.mapper;

import llc.devlab.app.model.dto.LabelCreateRequest;
import llc.devlab.app.model.dto.LabelDto;
import llc.devlab.app.model.dto.LabelUpdateRequest;
import llc.devlab.app.model.entity.Label;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LabelMapper {
    LabelDto toDto(Label label);

    List<LabelDto> toDtoList(List<Label> labels);

    Label toEntity(LabelCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(LabelUpdateRequest request, @MappingTarget Label label);
}
