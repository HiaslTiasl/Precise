package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.TaskType;

public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {
	
	Page<Task> findByModel(@Param("model") Model model, Pageable pageable);
	
	@RestResource(path = "all", rel = "all")
	@Query("SELECT t FROM Task t"
		+ " where (:model IS NULL OR t.model = :model)"
		+ " AND (:id IS NULL OR t.id = :id)"
		+ " AND (:phase IS NULL OR t.type.phase = :phase)"
		+ " AND (:type IS NULL OR t.type = :type)"
		+ " AND (:craft IS NULL OR t.type.craft = :craft)")
	Page<Task> findByAll(
		@Param("model") Model model,
		@Param("id") Long id,
		@Param("phase") Phase phase,
		@Param("type") TaskType type,
		@Param("craft") Craft craft,
		Pageable pageable
	);

}
