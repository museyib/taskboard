package llc.devlab.app.mapper;

import llc.devlab.app.model.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CsvFileMapper {

    public byte[] toCsv(List<Task> tasks) {
        StringBuilder csvContent = new StringBuilder("Id,Name,Description,ClosedAt,Status\n");
        for (Task task : tasks) {
            csvContent.append(task.getId()).append(",").append(task.getName()).append(",").append(task.getDescription()).append(",").append(task.getClosedAt()).append(",").append(task.getStatus()).append("\n");
        }
        return csvContent.toString().getBytes();
    }
}
