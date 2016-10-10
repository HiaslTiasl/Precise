package it.unibz.precise.rest;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
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
import it.unibz.precise.rep.ModelRepository;
import it.unibz.util.Util;

@RepositoryRestController
public class WarningsController {
	
	@Autowired
	private CycleChecker cycleChecker;
	
	@Autowired
	private ModelRepository modelRepository;
	
	@RequestMapping(path="/models/{id}/warnings", method=RequestMethod.GET)
	public ResponseEntity<?> getWarnings(@PathVariable("id") long id, PersistentEntityResourceAssembler ressourceAssembler) {
		Model model = modelRepository.findOne(id);
		if (model == null || ressourceAssembler == null)
			return ResponseEntity.notFound().build();
		List<ConsistencyWarning> warnings = cycleChecker.check(model);
		Function<BaseEntity, ?> entityMapper = ressourceAssembler::toFullResource;
		List<WarningResourceContent> projected = Util.mapToList(warnings, w -> new WarningResourceContent(w, entityMapper));
		
		return ResponseEntity.ok(new Resources<>(projected));
	}

}
