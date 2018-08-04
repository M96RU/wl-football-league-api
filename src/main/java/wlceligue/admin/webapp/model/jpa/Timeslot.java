package wlceligue.admin.webapp.model.jpa;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(nullable = false)
    public Instant date;

}
