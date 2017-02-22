package it.unibz.precise.rep;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import it.unibz.precise.model.Craft;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.Phase;
import it.unibz.precise.model.Task;
import it.unibz.precise.model.Activity;

/**
 * Repository for {@link Task}s.
 * 
 * @author MatthiasP
 *
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
	
	// TODO: remove?
	Page<Task> findByModel(@Param("model") Model model, Pageable pageable);
	
	/**
	 * Find tasks of the given model that match the given query text.
	 * The text is matched against the ID, the task's activity name, the phase name,
	 * and the craft name. If at least one of this matches succeeds, the task is returned.
	 * 
	 * Note: This is not a full text search.
	 * In particular, the query text is not split into multiple tokens at whitespaces etc.
	 * It is, however, matched case insensitive.
	 */
	@RestResource(path = "simple", rel = "simple")
	@Query("SELECT t FROM Task t LEFT JOIN t.activity.phase p LEFT JOIN t.activity.craft c"		// Left outer join needed to also consider tasks without craft or phase
		+ " WHERE t.model = :model"
		+ " AND ("
			+ " CAST(t.id as string) = :q"														// In case of ID, only show exact matches
			+ " OR (t.activity.name LIKE CONCAT('%', :q, '%'))"
			+ " OR (p IS NOT NULL AND p.name LIKE CONCAT('%', :q, '%'))"						// Must still explicitly handle NULL, otherwise whole WHERE clause 
			+ " OR (c IS NOT NULL AND c.name LIKE CONCAT('%', :q, '%'))"						// evaluates to NULL, and the task is ignored
		+ ")")
	Page<Task> searchSimple(
		@Param("model") Model model,
		@Param("q") String q,
		Pageable pageable
	);
	
	/** Find tasks that match all specified (non-null) criteria. */
	@RestResource(path = "advanced", rel = "advanced")
	@Query("SELECT t FROM Task t"
		+ " WHERE t.model = :model"
		+ " AND (:id IS NULL OR t.id = :id)"
		+ " AND (:phase IS NULL OR t.activity.phase = :phase)"
		+ " AND (:activity IS NULL OR t.activity = :activity)"
		+ " AND (:craft IS NULL OR t.activity.craft = :craft)")
	Page<Task> searchAdvanced(
		@Param("model") Model model,
		@Param("id") Long id,
		@Param("phase") Phase phase,
		@Param("activity") Activity activity,
		@Param("craft") Craft craft,
		Pageable pageable
	);

}
