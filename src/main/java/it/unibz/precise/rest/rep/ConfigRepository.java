package it.unibz.precise.rest.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.Configuration;

public interface ConfigRepository extends PagingAndSortingRepository<Configuration, Long> {

}
