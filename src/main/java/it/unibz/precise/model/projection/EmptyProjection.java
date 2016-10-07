package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.BaseEntity;

@Projection(name="empty", types=BaseEntity.class)
public interface EmptyProjection {

}
