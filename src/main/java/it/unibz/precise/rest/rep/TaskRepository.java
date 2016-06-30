package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Task;

public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {

}
