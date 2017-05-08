package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.restws;

import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.GradebookManager;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.model.Grade;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.model.GradedItem;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.jaxb.utils.Converter;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.GradebookUri;
import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Link;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Representation.*;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.security.SecureRandom;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bsenetza
 */
@Path("gradebook/item")
public class GradedItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final GradebookManager gradebook = GradebookManager.getInstance();
    private GradedItem gradedItem;

    @Context
    UriInfo uriInfo;

    /**
     * Creates a new instance of GradedItemResource
     */
    public GradedItemResource() {
        LOG.debug("Creating an GradedItem Resource");
    }

    @POST
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response createGradedItem(String content) {
        LOG.info("Creating the instance GradedItem {}", gradedItem);
        LOG.debug("POST request");
        LOG.debug("Request Content = {}", content);
        Response response;

        try {
            gradedItem = (GradedItem) Converter.convertFromXmlToObject(content, GradedItem.class);
            LOG.debug("The XML {} was converted to the object {}", content, gradedItem);

            if (gradebook.containsGradedItemId(gradedItem.getGradedItemId())) {
                LOG.info("Creating a {} {} Status Response", Response.Status.CONFLICT.getStatusCode(), Response.Status.CONFLICT.getReasonPhrase());
                LOG.debug("GradedItem already exists {}", gradedItem.getGradedItemId());
                String message = "GradedItem with " + gradedItem.getGradedItemId() + " already exists";
                response = Response.status(Response.Status.CONFLICT).entity(message).build();
            } else {
                gradedItem.setGradedItemId(generateId());
                while (gradebook.containsGradedItemId(gradedItem.getGradedItemId())) {
                    gradedItem.setGradedItemId(generateId());
                }

                gradebook.addGradedItem(gradedItem);

                gradebook.getGradedItems().forEach((gi) -> {
                    LOG.debug("gradedItemList member {}", gi);
                });

                URI locationURI = URI.create(uriInfo.getAbsolutePath() + "/" + Integer.toString(gradedItem.getGradedItemId()));

                gradedItem.addLink(new Link(RELATIONS_URI + SELF_REL_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_UPDATE_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_DELETE_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADE_CREATE_VALUE, new GradebookUri(locationURI, STUDENT_PATH)));

                String xmlString = Converter.convertFromObjectToXml(gradedItem, GradedItem.class);

                LOG.info("Creating a {} {} Status Response", Response.Status.CREATED.getStatusCode(), Response.Status.CREATED.getReasonPhrase());
                response = Response.status(Response.Status.CREATED).location(locationURI).entity(xmlString).build();
                gradedItem.getLink().clear();
            }

        } catch (JAXBException ex) {
            LOG.info("Creating a {} {} Status Response", Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.getReasonPhrase());
            LOG.debug("XML is {} is incompatible with GradedItem Resource", content);

            response = Response.status(Response.Status.BAD_REQUEST).entity(content).build();
        } catch (RuntimeException e) {
            LOG.debug("Catch All exception");

            LOG.info("Creating a {} {} Status Response", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());

            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(content).build();
        }

        LOG.debug("Generated response {}", response);

        return response;
    }

    @DELETE
    @Path("{gradedItemId}")
    public Response deleteGradedItem(@PathParam("gradedItemId") int gradedItemId) {
        LOG.info("Deleting the instance GradedItem {}", gradedItem);
        LOG.debug("DELETE request");
        LOG.debug("PathParam gradedItemId = {}", gradedItemId);

        Response response;

        if (!gradebook.containsGradedItemId(gradedItemId)) {
            LOG.info("Creating a {} {} Status Response", Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
            LOG.debug("Cannot find the GradedItem Resource");

            String message = "Graded Item Id " + String.valueOf(gradedItemId) + " not found";
            response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
        } else {
            LOG.info("Creating a {} {} Status Response", Response.Status.NO_CONTENT.getStatusCode(), Response.Status.NO_CONTENT.getReasonPhrase());
            LOG.debug("Deleting the GradedItem Resource {}", gradebook.getGradedItemByGradedItemId(gradedItemId));

            gradebook.removeGradedItemByGradedItemId(gradedItemId);

            response = Response.status(Response.Status.NO_CONTENT).build();
        }

        LOG.debug("Generated response {}", response);

        return response;
    }

    @PUT
    @Path("{gradedItemId}")
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response updateGradedItem(@PathParam("gradedItemId") int gradedItemId, String content) {
        LOG.info("Updating the instance GradedItem {}", gradedItem);
        LOG.debug("PUT request");
        LOG.debug("Request Content = {}", content);

        Response response;
        gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);

        if (gradedItem == null) {
            LOG.info("Creating a {} {} Status Response", Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
            LOG.debug("Cannot find the GradedItem Resource");

            String message = "Graded Item Id " + String.valueOf(gradedItemId) + " not found";
            response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
        } else {
            LOG.debug("Updating the GradedItem Resource {}", gradedItem);
            try {
                GradedItem updatedGradedItem = (GradedItem) Converter.convertFromXmlToObject(content, GradedItem.class);
                LOG.debug("The XML {} was converted to the object {}", content, updatedGradedItem);
                LOG.debug("Removing the GradedItem Resource {}", gradedItem);
                gradebook.removeGradedItemByGradedItemId(gradedItemId);
                LOG.debug("Adding the GradedItem Resource {}", updatedGradedItem);
                gradebook.addGradedItem(updatedGradedItem);
                gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);

                URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                gradedItem.addLink(new Link(RELATIONS_URI + SELF_REL_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_UPDATE_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_DELETE_VALUE, new GradebookUri(locationURI)));
                gradedItem.addLink(new Link(RELATIONS_URI + GRADE_CREATE_VALUE, new GradebookUri(locationURI, STUDENT_PATH)));

                LOG.info("Creating a {} {} Status Response", Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase());
                String xmlString = Converter.convertFromObjectToXml(gradedItem, GradedItem.class);

                response = Response.status(Response.Status.OK).entity(xmlString).build();
                gradedItem.getLink().clear();
            } catch (JAXBException ex) {
                LOG.info("Creating a {} {} Status Response", Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.getReasonPhrase());
                LOG.debug("XML is {} is incompatible with GradedItem Resource", content);

                response = Response.status(Response.Status.BAD_REQUEST).entity(content).build();
            } catch (RuntimeException e) {
                LOG.debug("Catch All exception");

                LOG.info("Creating a {} {} Status Response", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());

                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(content).build();
            }
        }
        LOG.debug("Generated response {}", response);
        return response;
    }

    @GET
    @Path("{gradedItemId}")
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response getGradedItem(@PathParam("gradedItemId") int gradedItemId) {
        LOG.info("Getting the instance GradedItem {}", gradedItem);
        LOG.debug("GET request");

        Response response;

        gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);

        if (gradedItem == null) {
            LOG.info("Creating a {} {} Status Response", Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
            LOG.debug("Cannot find the GradedItem Resource");

            String message = "Graded Item Id " + String.valueOf(gradedItemId) + " not found";
            response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
        } else {
            LOG.info("Creating a {} {} Status Response", Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase());
            LOG.debug("Retrieving the GradedItem Resource {}", gradedItem);
            URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
            gradedItem.addLink(new Link(RELATIONS_URI + SELF_REL_VALUE, new GradebookUri(locationURI)));
            gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_UPDATE_VALUE, new GradebookUri(locationURI)));
            gradedItem.addLink(new Link(RELATIONS_URI + GRADEDITEM_DELETE_VALUE, new GradebookUri(locationURI)));
            gradedItem.addLink(new Link(RELATIONS_URI + GRADE_CREATE_VALUE, new GradebookUri(locationURI, STUDENT_PATH)));
            gradedItem.getGrade().forEach((grade) -> {
                gradedItem.addLink(new Link(RELATIONS_URI + GRADE_SELF_VALUE, new GradebookUri(locationURI, STUDENT_PATH + "/" + grade.getStudentId())));
            });

            String xmlString = Converter.convertFromObjectToXml(gradedItem, GradedItem.class);

            response = Response.status(Response.Status.OK).entity(xmlString).build();
            gradedItem.getLink().clear();
        }

        LOG.debug("Generated response {}", response);

        return response;
    }

    private int generateId() {
        SecureRandom random = new SecureRandom();
        return random.nextInt(1000);
    }
}
