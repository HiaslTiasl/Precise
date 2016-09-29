package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.TaskType;

public interface TaskTypeRepository extends PagingAndSortingRepository<TaskType, Long> {
	
	Page<TaskType> findByModel(@Param("model") Model model, Pageable p);
	
	Page<TaskType> findByPhase(@Param("phase") Phase phase, Pageable p);

}
