package wlceligue.admin.webapp.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PK", nullable = false)
    public Integer id;

    @Column(name = "DAS", nullable = false)
    public String das;

    @Column(name = "FN", nullable = false)
    public String firstname;

    @Column(name = "LN", nullable = false)
    public String lastname;

    public boolean admin;

    public String email;

}
