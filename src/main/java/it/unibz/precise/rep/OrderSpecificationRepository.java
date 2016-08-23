package it.unibz.precise.rep;

import org.springframework.data.repository.PagingAndSortingRepository;

import it.unibz.precise.model.OrderSpecification;

public interface OrderSpecificationRepository extends PagingAndSortingRepository<OrderSpecification, Long> {

}
