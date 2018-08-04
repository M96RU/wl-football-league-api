package wlceligue.admin.webapp.model.jpa;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class UserAvailability {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@ManyToOne(optional = false)
	public User user;

    @Column(nullable = false)
    public Instant date;

}
