
package pl.sylwekczmil.timetableserver.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Entity
@Table(name = "timetable")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Timetable.findAll", query = "SELECT t FROM Timetable t"),
    @NamedQuery(name = "Timetable.findByIdTimetable", query = "SELECT t FROM Timetable t WHERE t.idTimetable = :idTimetable"),
    @NamedQuery(name = "Timetable.findByName", query = "SELECT t FROM Timetable t WHERE t.name = :name"),
    @NamedQuery(name = "Timetable.findByWeek", query = "SELECT t FROM Timetable t WHERE t.week = :week"),
    @NamedQuery(name = "Timetable.findEvents", query = "SELECT e FROM Timetable t JOIN t.eventCollection e WHERE e.idTimetable.idTimetable = :idTimetable")})
public class Timetable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_timetable")
    private Integer idTimetable;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "week")
    private int week;
    @JoinTable(name = "user_timetable", joinColumns = {
        @JoinColumn(name = "id_timetable", referencedColumnName = "id_timetable")}, inverseJoinColumns = {
        @JoinColumn(name = "id_user", referencedColumnName = "id_user")})
    @ManyToMany
    private Collection<User> userCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idTimetable")
    private Collection<Event> eventCollection;

    public Timetable() {
    }

    public Timetable(Integer idTimetable) {
        this.idTimetable = idTimetable;
    }

    public Timetable(Integer idTimetable, String name, int week) {
        this.idTimetable = idTimetable;
        this.name = name;
        this.week = week;
    }

    public Integer getIdTimetable() {
        return idTimetable;
    }

    public void setIdTimetable(Integer idTimetable) {
        this.idTimetable = idTimetable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    
    public Collection<User> getUserCollection() {
        return userCollection;
    }

    public void setUserCollection(Collection<User> userCollection) {
        this.userCollection = userCollection;
    }

    @XmlTransient
    public Collection<Event> getEventCollection() {
        return eventCollection;
    }

    public void setEventCollection(Collection<Event> eventCollection) {
        this.eventCollection = eventCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTimetable != null ? idTimetable.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Timetable)) {
            return false;
        }
        Timetable other = (Timetable) object;
        if ((this.idTimetable == null && other.idTimetable != null) || (this.idTimetable != null && !this.idTimetable.equals(other.idTimetable))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pl.sylwekczmil.timetableserver.Timetable[ idTimetable=" + idTimetable + " ]";
    }

}
