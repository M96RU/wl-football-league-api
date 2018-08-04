package wlceligue.admin.webapp.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "season")
public class Season {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PK", nullable = false)
	public Integer id;

	@Column(name = "LABEL", nullable = false)
	public String label;

	@Column(name = "DIVISION", nullable = false)
	public Integer division;

	@Column(name = "CUP", nullable = false)
	public Integer cup;

}
