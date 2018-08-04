package wlceligue.admin.webapp.model.jpa;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Baton {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	@ManyToOne(optional = false)
	public User user;

    @ManyToOne
    public User lostAgainst;

	@ManyToOne
	public Match wonGame;

	@ManyToOne
	public Match lostGame;

	@Column(nullable = false)
	public Instant wonDate;

	@Column
	public Instant lostDate;

	@Column
	public long sinceGames = 0;

    public boolean forced;

}
