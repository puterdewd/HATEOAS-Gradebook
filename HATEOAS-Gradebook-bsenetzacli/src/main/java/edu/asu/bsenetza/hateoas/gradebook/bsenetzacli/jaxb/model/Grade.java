package edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.jaxb.model;

import static edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation.Representation.GRADEBOOK_NAMESPACE;
import java.lang.invoke.MethodHandles;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bsenetza
 */
@XmlRootElement
@XmlType(propOrder = {
    "studentId",
    "score",
    "comment"})
/**
 *
 * @author bsenetza
 */
public class Grade {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int studentId;
    private float score;
    private String comment;

    public Grade() {
        LOG.info("Creating a Grade object");
    }

    public int getStudentId() {
        return studentId;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setStudentId(int studentId) {
        LOG.info("Setting the studentId to {}", studentId);

        this.studentId = studentId;

        LOG.debug("The updated Grade = {}", this);
    }

    public String getComment() {
        return comment;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setComment(String comment) {
        LOG.info("Setting the comment to {}", comment);

        this.comment = comment;

        LOG.debug("The updated Grade = {}", this);
    }

    public float getScore() {
        return score;
    }

    @XmlElement(namespace = GRADEBOOK_NAMESPACE)
    public void setScore(float score) {
        LOG.info("Setting the score to {}", score);

        this.score = score;

        LOG.debug("The updated Grade = {}", this);
    }

    @Override
    public String toString() {
        return "Grade{" + "studentId=" + studentId + ", score=" + score + ", comment=" + comment + '}';
    }
}
