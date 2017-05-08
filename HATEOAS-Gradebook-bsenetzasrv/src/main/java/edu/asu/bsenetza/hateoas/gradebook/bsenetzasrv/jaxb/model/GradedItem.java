package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.model;

import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Link;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Representation;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Representation.DAP_NAMESPACE;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Representation.GRADEBOOK_NAMESPACE;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bsenetza
 */
@XmlRootElement(namespace = Representation.GRADEBOOK_NAMESPACE)
@XmlType(propOrder = {
    "gradedItemId",
    "description",
    "percentage",
    "grade",
    "link"})
public class GradedItem {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int gradedItemId;
    private String description;
    private float percentage;
    private List<Grade> grade = new ArrayList<>();
    private List<Link> link = new ArrayList<>();

    //   private Grade grade;
    public GradedItem() {
        LOG.info("Creating a GradedItem object");
    }

    public int getGradedItemId() {
        return gradedItemId;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setGradedItemId(int gradedItemId) {
        LOG.info("Setting the gradedItemId to {}", gradedItemId);

        this.gradedItemId = gradedItemId;

        LOG.debug("The updated GradedItem = {}", this);
    }

    public String getDescription() {
        return description;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setDescription(String description) {
        LOG.info("Setting the description to {}", description);

        this.description = description;

        LOG.debug("The updated GradedItem = {}", this);
    }

    public float getPercentage() {
        return percentage;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setPercentage(float percentage) {
        LOG.info("Setting the percentage to {}", percentage);

        this.percentage = percentage;

        LOG.debug("The updated GradedItem = {}", this);
    }

    public List<Grade> getGrade() {
        return grade;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setGrade(List<Grade> grade) {
        this.grade = grade;
    }

    public void addGrade(Grade grade) {
        LOG.info("Adding the grade {}", grade);

        this.grade.add(grade);
    }

    
    public List<Link> getLink() {
        return link;
    }
    
    @XmlElement(namespace = DAP_NAMESPACE)
    public void setLink(List<Link> link) {
        this.link = link;
    }

    public void addLink(Link link) {
        LOG.info("Adding the link {}", link);

        this.link.add(link);
    }
    
    public boolean containsStudentId(int studentId) {
        for (Grade grade : grade) {
            if (studentId == grade.getStudentId()) {
                return true;
            }
        }
        return false;
    }

    public Grade getGradeByStudentId(int studentId) {
        for (Grade grade : grade) {
            if (studentId == grade.getStudentId()) {
                return grade;
            }
        }
        return null;
    }

    public void removeGradeByStudentId(int studentId) {
        for (Grade grade : grade) {
            if (studentId == grade.getStudentId()) {
                this.grade.remove(grade);
                return;
            }
        }
    }

    @Override
    public String toString() {

        return "GradedItem{"
                + "gradedItemId=" + gradedItemId
                + ", description=" + description
                + ", percentage=" + percentage
                + ", grade=" + grade
                + ", link=" + link
                + '}';
    }
}
