package edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.representation;

import edu.asu.bsenetza.hateoas.gradebook.bsenetzacli.jaxb.model.GradedItem;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Representation {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String RELATIONS_URI = "http://relations.gradebook.com/";
    public static final String GRADEBOOK_NAMESPACE = "http://schemas.gradebook.com";
    public static final String DAP_NAMESPACE = GRADEBOOK_NAMESPACE + "/dap";
    public static final String GRADEBOOK_MEDIA_TYPE = "application/vnd.gradebook+xml";
    public static final String STUDENT_PATH = "student";
    public static final String SELF_REL_VALUE = "self";
    public static final String GRADEDITEM_UPDATE_VALUE = "update";
    public static final String GRADEDITEM_DELETE_VALUE = "delete";
    public static final String GRADE_CREATE_VALUE = "grade_create";
    public static final String GRADE_SELF_VALUE = "grade_self";
    public static final String GRADE_UPDATE_VALUE = "grade_update";
    public static final String GRADE_DELETE_VALUE = "grade_delete";

    public static final List<Link> links = new ArrayList<>();

    public static void setLinks(GradedItem gradedItem) {
        clearLinks();
        for (Link link : gradedItem.getLink()) {
            links.add(link);
        }
    }

    public static void addLink(Link link) {
        links.add(link);
    }

    public static List<Link> getLinks() {
        return links;
    }

    public static void clearLinks() {
        links.clear();
    }

    public static boolean linksContainsRelValue(String relValue) {
        for (Link l : links) {
            if (l.getRelValue().toLowerCase().equals(relValue.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static Link getSelfLink() {
        return getLinkByRelValue(RELATIONS_URI + SELF_REL_VALUE);
    }

    public static Link getGradedItemUpdateLink() {
        return getLinkByRelValue(RELATIONS_URI + GRADEDITEM_UPDATE_VALUE);
    }

    public static Link getGradedItemDeleteLink() {
        return getLinkByRelValue(RELATIONS_URI + GRADEDITEM_DELETE_VALUE);
    }

    public static Link getGradeSelfLink(String studentId) {
        for (Link l : links) {
            if (l.getRelValue().toLowerCase().equals(RELATIONS_URI + GRADE_SELF_VALUE)
                    && l.getUri().toString().endsWith(studentId)) {
                return l;
            }
        }
        return null;
    }

    public static Link getGradeCreateLink() {
        return getLinkByRelValue(RELATIONS_URI + GRADE_CREATE_VALUE);
    }

    public static Link getGradeUpdateLink() {
        return getLinkByRelValue(RELATIONS_URI + GRADE_UPDATE_VALUE);
    }

    public static Link getGradeDeleteLink() {
        return getLinkByRelValue(RELATIONS_URI + GRADE_DELETE_VALUE);
    }

    public static boolean isGradedItemSelf(String gradedItemId) {
        return getSelfLink().getUri().toString().endsWith(gradedItemId);
    }
    
    public static boolean isGradeSelf(String studentId) {
        return getGradeSelfLink(studentId) != null;
    }

    public static Link getSelfFromGradeSelf() {
        Link link = getLinkByRelValue(RELATIONS_URI + GRADE_SELF_VALUE);
        String path = link.getUri().toString();
        int index = path.indexOf("/" + STUDENT_PATH);
        URI locationURI = URI.create(path.substring(0, index));
        return new Link(RELATIONS_URI + SELF_REL_VALUE, new GradebookUri(locationURI));
    }

    private static Link getLinkByRelValue(String relValue) {
        for (Link l : links) {
            if (l.getRelValue().toLowerCase().equals(relValue.toLowerCase())) {
                return l;
            }
        }
        return null;
    }
}
