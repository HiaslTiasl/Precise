package it.unibz.precise.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
	@UniqueConstraint(columnNames={"model_id", "name"})
})
public class Craft extends BaseEntity implements ShortNameProvider {
	
	@Column(nullable=false)
	private String name;
	private String shortName;
	
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
	
}
