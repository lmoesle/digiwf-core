package de.muenchen.oss.digiwf.task.service.domain;

import io.holunda.polyflow.view.Task;
import lombok.Data;

import java.util.List;

@Data
public class PageOfTasks {
  private final List<Task> tasks;
  private final Integer totalElementsCount;
  private final PagingAndSorting pagingAndSorting;
}
