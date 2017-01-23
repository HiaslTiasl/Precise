package it.unibz.precise.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.unibz.precise.model.LocationPatterns;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Phase;
import it.unibz.precise.rep.TaskRepository;

/**
 * Exposes {@link LocationPatterns#checkPattern(Map, Phase)}.
 * 
 * Allows a client to send a pattern to be checked and receive back
 * the same pattern with allowedValues updated and invalid values set
 * to wildcard.
 * 
 * @author MatthiasP
 *
 */
@RepositoryRestController
public class CheckedPatternController {
	
	@Autowired
	private TaskRepository taskRepository;
	
	@RequestMapping(
		path="/tasks/{id}/checkedPattern",
		method=RequestMethod.PUT			// To indicate that the operation is idempotent since nothing is persisted
	)
	public ResponseEntity<Map<String, PatternEntry>> checkedPatternForTask(
		@PathVariable("id") long id,
		@RequestBody Map<String, PatternEntry> pattern)
	{
		Phase phase = taskRepository.findOne(id).getType().getPhase();
		return phase == null
			? ResponseEntity.notFound().build()
			: ResponseEntity.ok(LocationPatterns.checkPattern(pattern, phase));
	}
	
	

}
