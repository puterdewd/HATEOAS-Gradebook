package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv;

import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.model.GradedItem;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Threadsafe Singleton used to manage Gradebook
 * @author bsenetza
 */
public class GradebookManager {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final GradebookManager instance = new GradebookManager();
    private static List<GradedItem> gradedItems;

    public static GradebookManager getInstance() {
        return instance;
    }

    private GradebookManager() {
        gradedItems = new ArrayList<>();
    }

    public void addGradedItem(GradedItem gradedItem) throws IllegalArgumentException {
        LOG.debug("Adding a GradedItem object {}", gradedItem);
        if (gradedItem == null) {
            throw new IllegalArgumentException("Adding null entry to Gradebook");
        }
        gradedItems.add(gradedItem);
    }



    public boolean containsGradedItemId(int gradedItemId) {
        for (GradedItem gradedItem : gradedItems) {
            if (gradedItemId == gradedItem.getGradedItemId()) {
                return true;
            }
        }
        return false;
    }

    public GradedItem getGradedItemByGradedItemId(int gradedItemId) {
        for (GradedItem gradedItem : gradedItems) {
            if (gradedItemId == gradedItem.getGradedItemId()) {
                return gradedItem;
            }
        }
        return null;
    }

    public void removeGradedItemByGradedItemId(int gradedItemId) {
        for (GradedItem gradedItem : gradedItems) {
            if (gradedItemId == gradedItem.getGradedItemId()) {
                gradedItems.remove(gradedItem);
                return;
            }
        }
    }

    public List<GradedItem> getGradedItems() {
        LOG.debug("Listing {} gradedItems ", gradedItems.size());
        List<GradedItem> gradedItemList = new ArrayList<>();

        gradedItems.forEach((gradedItem) -> {
            GradedItem giCopy = new GradedItem();
            giCopy.setDescription(gradedItem.getDescription());
            giCopy.setGradedItemId(gradedItem.getGradedItemId());
            giCopy.setPercentage(gradedItem.getPercentage());
            gradedItemList.add(giCopy);
        });
        return gradedItemList;
    }

}
