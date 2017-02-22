package it.unibz.precise.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Represents a type of workers.
 * 
 * @author MatthiasP
 *
 */
@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(name=Craft.UC_NAME, columnNames={"model_id", "name"}),
	@UniqueConstraint(name=Craft.UC_SHORTNAME, columnNames={"model_id", "shortName"})
})
public class Craft extends BaseEntity implements ShortNameProvider {
	
	public static final String UC_NAME = "UC_CRAFT_NAME";
	public static final String UC_SHORTNAME = "UC_CRAFT_SHORTNAME";
	
	@Column(nullable=false)
	@NotNull(message="{craft.name.required}")
	private String name;
	private String shortName;
	private String description;
	
	@ManyToOne
	private Model model;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		ModelToMany.CRAFTS.setOne(this, model);
	}

	void internalSetModel(Model model) {
		this.model = model;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
