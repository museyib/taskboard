package llc.devlab.app.repository;

import llc.devlab.app.model.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findAllByDeletedFalse();

    Optional<Label> findByIdAndDeletedFalse(Long id);

    Label findByName(String name);
}
