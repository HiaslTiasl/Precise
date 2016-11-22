package it.unibz.precise.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.unibz.precise.model.LocationPatterns;
import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Phase;
import it.unibz.precise.rep.PhaseRepository;
import it.unibz.precise.rep.TaskRepository;

@RepositoryRestController
public class CheckedPatternController {
	
	@Autowired
	private PhaseRepository phaseRepository;
	@Autowired
	private TaskRepository taskRepository;
	
	private ResponseEntity<Map<String, PatternEntry>> checkedPattern(Phase phase, Map<String, PatternEntry> pattern) {
		return phase == null
			? new ResponseEntity<>(HttpStatus.NOT_FOUND)
			: new ResponseEntity<>(LocationPatterns.checkPattern(pattern, phase), HttpStatus.OK);
	}
	
	@RequestMapping(path="/phases/{id}/checkedPattern", method=RequestMethod.PUT)
	public ResponseEntity<Map<String, PatternEntry>> checkedPatternForPhase(
		@PathVariable("id") long id,
		@RequestBody Map<String, PatternEntry> pattern)
	{
		return checkedPattern(phaseRepository.findOne(id), pattern);
	}
	
	@RequestMapping(path="/tasks/{id}/checkedPattern", method=RequestMethod.PUT)
	public ResponseEntity<Map<String, PatternEntry>> checkedPatternForTask(
			@PathVariable("id") long id,
			@RequestBody Map<String, PatternEntry> pattern)
	{
		return checkedPattern(taskRepository.findOne(id).getType().getPhase(), pattern);
	}
	
	

}
