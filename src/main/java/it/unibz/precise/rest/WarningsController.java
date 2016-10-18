package it.unibz.precise.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.unibz.precise.check.ConsistencyWarning;
import it.unibz.precise.check.CycleChecker;
import it.unibz.precise.model.BaseEntity;
import it.unibz.precise.model.Model;
import it.unibz.precise.model.projection.EmptyProjection;
import it.unibz.precise.rep.ModelRepository;
import it.unibz.util.Util;

@RepositoryRestController
public class WarningsController {
	
	@Autowired
	private CycleChecker cycleChecker;
	
	@Autowired
	private ModelRepository modelRepository;
	
	@Autowired
	private ProjectionFactory projectionFactory;
	
	@RequestMapping(path="/models/{id}/warnings", method=RequestMethod.GET)
	public ResponseEntity<?> getWarnings(@PathVariable("id") long id) {
		Model model = modelRepository.findOne(id);
		if (model == null)
			return ResponseEntity.notFound().build();
		List<ConsistencyWarning> warnings = cycleChecker.check(model);
		List<WarningResourceContent> projected = Util.mapToList(warnings, w -> new WarningResourceContent(w, this::mapEntity));
		
		return ResponseEntity.ok(new Resources<>(projected));
	}
	
	private EmptyProjection mapEntity(BaseEntity e) {
		return projectionFactory.createProjection(EmptyProjection.class, e);
	}

}
