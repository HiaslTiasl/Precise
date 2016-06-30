package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.TaskType;

public interface TaskTypeRepository extends PagingAndSortingRepository<TaskType, Long> {

}
