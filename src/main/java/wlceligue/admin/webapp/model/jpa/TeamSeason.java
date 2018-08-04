package wlceligue.admin.webapp.model.jpa;

import com.querydsl.core.util.MathUtils;
import java.math.BigDecimal;
import javax.persistence.*;

@Entity
@Table(name = "teamseason")
public class TeamSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PK", nullable = false)
    public Integer id;

    @ManyToOne
    @JoinColumn(name = "TEAM_FK", nullable = false)
    public Team team;

    @ManyToOne
    @JoinColumn(name = "SEASON_FK", nullable = false)
    public Season season;

    @Column(name = "DIVISION", nullable = false)
    public Integer division;

    // Stats
    @Column(name = "ATTACK", nullable = false)
    public Integer attack = 0;

    @Column(name = "MIDFIELD", nullable = false)
    public Integer midfield = 0;

    @Column(name = "DEFENCE", nullable = false)
    public Integer defence = 0;

    @Transient
    public BigDecimal getOverall() {
        return new BigDecimal((attack + midfield + defence) / 3.0).setScale(1, BigDecimal.ROUND_HALF_UP);
    }
}
