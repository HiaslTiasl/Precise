package it.unibz.precise.rep;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import it.unibz.precise.model.Location;
import it.unibz.precise.model.Model;

public interface LocationRepository extends PagingAndSortingRepository<Location, Long> {
	
	List<Location> findByTask_Model(@Param("model") Model model);

}
