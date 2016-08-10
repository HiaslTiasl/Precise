package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.TaskType;

public interface TaskTypeRepository extends PagingAndSortingRepository<TaskType, Long> {

}
