package wlceligue.admin.webapp.model.jpa;

import java.time.Instant;

import javax.persistence.*;

import wlceligue.admin.webapp.enums.Compet;
import wlceligue.admin.webapp.enums.MatchStatus;

@Entity
@Table(name = "matches")
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PK", nullable = false)
	public Integer id;

	@Version
    public int version;

	@ManyToOne
	@JoinColumn(name = "SEASON_FK", nullable = false)
	public Season season;

	@Column(name = "COMPET", nullable = false)
	@Enumerated(EnumType.STRING)
	public Compet compet;

	@Column(name = "ROUND", nullable = false)
	public Integer round;

	@Column(name = "DAY", nullable = false)
	public Integer day;

	@Column(name = "STATUS", nullable = false)
	@Enumerated(EnumType.STRING)
	public MatchStatus status;

	@Column(name = "DATE")
	public Instant date;

	@ManyToOne
	@JoinColumn(name = "U1")
	public User user1;

	@ManyToOne
	@JoinColumn(name = "T1")
	public Team team1;

	@ManyToOne
	@JoinColumn(name = "U2")
	public User user2;

	@ManyToOne
	@JoinColumn(name = "T2")
	public Team team2;

	@Column(name = "S1")
	public Integer score1;

	@Column(name = "S2")
	public Integer score2;

	@Column(name = "P1")
	public Integer prolongation1;

	@Column(name = "P2")
	public Integer prolongation2;

	@Column(name = "TAB1")
	public Integer tab1;

	@Column(name = "TAB2")
	public Integer tab2;

}
