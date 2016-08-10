package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Task;

public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {

}
