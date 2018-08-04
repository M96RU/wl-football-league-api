package wlceligue.admin.webapp.model.jpa;

import javax.persistence.*;

@Entity
@Table(name = "userseason")
public class UserSeason {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PK", nullable = false)
	public Integer id;

	@ManyToOne
	@JoinColumn(name = "USER_FK", nullable = false)
	public User user;

	@ManyToOne
	@JoinColumn(name = "SEASON_FK", nullable = false)
	public Season season;

	@Column(name = "DIVISION", nullable = false)
	public Integer division;

	// Ranking
	@Column(name = "CHOICE", nullable = false)
	public Integer choice = 0;

	@ManyToOne
	@JoinColumn(name = "TEAM_FK", nullable = true)
	public Team team;

	// Ranking
	@Column(name = "RANK", nullable = false)
	public Integer rank = 0;

	@Column(name = "GOALS", nullable = false)
	public Integer goalFor = 0;

	@Column(name = "AGAINST", nullable = false)
	public Integer goalAgainst = 0;

	@Column(name = "WON", nullable = false)
	public Integer won = 0;

	@Column(name = "DRAW", nullable = false)
	public Integer draw = 0;

	@Column(name = "LOSE", nullable = false)
	public Integer lose = 0;

	// calculated
	@Column(name = "GA", nullable = false)
	public Integer goalAverage = 0;

	@Column(name = "POINTS", nullable = false)
	public Integer points = 0;

	@Column(name = "POTENTIAL_POINTS", nullable = false)
	public Integer potentialPoints = 0;

	@Column(name = "LEAGUE_WINNER", nullable = false)
	public boolean leagueWinner = false;

	// Cup
	@Column(name = "CUP_TOUR", nullable = false)
	public Integer cupRound = 0;

	@Column(name = "CUP_IN_PROGRESS", nullable = false)
	public boolean cupInProgress = false;

	@Column(name = "CUP_WINNER", nullable = false)
	public boolean cupWinner = false;

	@Column(name = "CUP_GOALS", nullable = false)
	public Integer cupGoalFor = 0;

	@Column(name = "CUP_AGAINST", nullable = false)
	public Integer cupGoalAgainst = 0;

	@Column(name = "CUP_WON", nullable = false)
	public Integer cupWon = 0;

	@Column(name = "CUP_LOSE", nullable = false)
	public Integer cupLose = 0;

	@Column(name = "CUP_WON_P", nullable = false)
	public Integer cupWonProlongation = 0;

	@Column(name = "CUP_LOSE_P", nullable = false)
	public Integer cupLoseProlongation = 0;

	@Column(name = "CUP_WON_TAB", nullable = false)
	public Integer cupWonTab = 0;

	@Column(name = "CUP_LOSE_TAB", nullable = false)
	public Integer cupLoseTab = 0;

	@Transient
	public Integer getPlayed() {
		return won + draw + lose;
	}

}
