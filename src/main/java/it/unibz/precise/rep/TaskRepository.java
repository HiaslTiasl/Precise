package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import it.unibz.precise.model.Task;
import it.unibz.precise.model.projection.TaskProjection;

@RepositoryRestResource(excerptProjection=TaskProjection.class)
public interface TaskRepository extends PagingAndSortingRepository<Task, Long> {

}
