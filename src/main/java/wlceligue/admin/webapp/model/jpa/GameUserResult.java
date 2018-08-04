package wlceligue.admin.webapp.model.jpa;

import wlceligue.admin.webapp.enums.Compet;
import wlceligue.admin.webapp.enums.GameOutcome;
import wlceligue.admin.webapp.enums.MatchStatus;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class GameUserResult {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

    @ManyToOne(optional = false)
    public Match game;

    @ManyToOne(optional = false)
    public User user;

    @ManyToOne(optional = false)
    public Team userTeam;

    @ManyToOne(optional = false)
    public User against;

    @ManyToOne(optional = false)
    public Team againstTeam;

	@ManyToOne(optional = false)
	public Season season;

    @Column(nullable = false)
    public Instant date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public MatchStatus status;

    @Column(nullable = false)
	@Enumerated(EnumType.STRING)
	public Compet compet;

	public Integer cupRound;

	public Integer leagueDay;

    public Integer division;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
	public GameOutcome outcome;

	public Boolean win;
    public Boolean lose;
    public Boolean draw;

	public Integer goalsFor;
    public Integer goalsAgainst;

    public Integer regulationTimeFor;
    public Integer regulationTimeAgainst;

    public Integer overtimeFor;
    public Integer overtimeAgainst;

    public Integer tabFor;
	public Integer tabAgainst;

	public Integer goalAverage;

    public Boolean winRegulationTime;
    public Boolean winOvertime;
    public Boolean winTab;

    public Boolean loseRegulationTime;
    public Boolean loseOvertime;
    public Boolean loseTab;
}
