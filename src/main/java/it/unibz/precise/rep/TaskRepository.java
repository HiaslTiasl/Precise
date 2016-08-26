package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Task;

public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {
	
	Page<Task> findByModel(@Param("model") Model model, Pageable pageable);

}
