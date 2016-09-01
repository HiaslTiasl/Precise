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

import it.unibz.precise.model.PatternEntry;
import it.unibz.precise.model.Task;
import it.unibz.precise.rep.TaskRepository;

@RepositoryRestController
public class CheckedPatternController {
	
	@Autowired
	private TaskRepository repository;
	
	@RequestMapping(path="tasks/{id}/checkedPattern", method=RequestMethod.PUT)
	public ResponseEntity<Map<String, PatternEntry>> getCheckedPattern(
		@PathVariable("id") long id,
		@RequestBody Map<String, PatternEntry> pattern)
	{
		Task task = repository.findOne(id);
		if (task != null)
			return new ResponseEntity<>(task.checkPattern(pattern), HttpStatus.OK);
		return null;
	}
	

}
