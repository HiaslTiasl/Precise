package it.unibz.precise.model.projection;

import org.springframework.data.rest.core.config.Projection;

import it.unibz.precise.model.BaseEntity;

/**
 * Projection that only contains links but no properties.
 * Can be used with all persistent entities.
 * 
 * @author MatthiasP
 *
 */
@Projection(name="empty", types=BaseEntity.class)
public interface EmptyProjection {

}
