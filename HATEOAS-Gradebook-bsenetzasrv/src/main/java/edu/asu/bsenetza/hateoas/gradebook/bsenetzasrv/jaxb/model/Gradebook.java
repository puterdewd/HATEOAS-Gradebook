package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.model;

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
@XmlRootElement
@XmlType(propOrder = {
    "gradedItem"})
public class Gradebook {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private List<GradedItem> gradedItem = new ArrayList<>();

    //   private Grade grade;
    public Gradebook() {
        LOG.info("Creating a GradedItem object");
    }


    public List<GradedItem> getGradedItem() {
        return gradedItem;
    }

    public void setGradedItem(List<GradedItem> gradedItem) {
        this.gradedItem = gradedItem;
    }

 

    @Override
    public String toString() {

        return "Gradedbook{"
                + "gradedItem=" + gradedItem
                + '}';
    }
}
