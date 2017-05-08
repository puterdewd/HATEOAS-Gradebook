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
@Path("gradebook/item/{gradedItemId}/student/{studentId}")
public class GradeResource {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final GradebookManager gradebook = GradebookManager.getInstance();
    private Grade grade;
    private GradedItem gradedItem;

    @Context
    UriInfo uriInfo;

    /**
     * Creates a new instance of GradeResource
     */
    public GradeResource() {
        LOG.info("Creating a Grade Resource");
    }

    @POST
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response createGrade(@PathParam("gradedItemId") int gradedItemId, @PathParam("studentId") int studentId, String content) {
        LOG.info("Creating the instance Grade {}", grade);
        LOG.debug("POST request");
        LOG.debug("Request Content = {}", content);

        Response response;

        try {
            LOG.debug("gradedItemId exists? {}", gradebook.containsGradedItemId(gradedItemId));
            if (!gradebook.containsGradedItemId(gradedItemId)) {
                LOG.info("Creating a {} {} Status Response", Response.Status.PRECONDITION_FAILED.getStatusCode(), Response.Status.PRECONDITION_FAILED.getReasonPhrase());
                LOG.debug("gradedItemId does not exist {}", content);
                String message = "GradedItem " + gradedItemId + " does not exist. Item must be created before creating grades.";
                URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                response = Response.status(Response.Status.PRECONDITION_FAILED).location(locationURI).entity(message).build();
            } else {
                if (gradebook.getGradedItemByGradedItemId(gradedItemId).containsStudentId(studentId)) {
                    LOG.info("Creating a {} {} Status Response", Response.Status.CONFLICT.getStatusCode(), Response.Status.CONFLICT.getReasonPhrase());
                    LOG.debug("Grade with studentId already exists {}", studentId);
                    String message = "Grade for student Id " + studentId + " already exists";
                    response = Response.status(Response.Status.CONFLICT).entity(message).build();
                } else {
                    LOG.info("Creating a {} {} Status Response", Response.Status.CREATED.getStatusCode(), Response.Status.CREATED.getReasonPhrase());
                    LOG.debug("Adding Grade to GradedItem", content);
                    gradedItem = (GradedItem) Converter.convertFromXmlToObject(content, GradedItem.class);
                    grade = gradedItem.getGrade().get(0);
                    LOG.debug("Grade {}", grade);

                    gradebook.getGradedItemByGradedItemId(gradedItemId).addGrade(grade);
                    gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);
                    LOG.debug("GradedItem {}", gradedItem);

                    URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_SELF_VALUE, new GradebookUri(locationURI)));
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_UPDATE_VALUE, new GradebookUri(locationURI)));
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_DELETE_VALUE, new GradebookUri(locationURI)));

                    String xmlString = Converter.convertFromObjectToXml(gradedItem, GradedItem.class);
                    response = Response.status(Response.Status.CREATED).location(locationURI).entity(xmlString).build();
                    gradedItem.getLink().clear();
                }
            }
        } catch (JAXBException ex) {
            LOG.info("Creating a {} {} Status Response", Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.getReasonPhrase());
            LOG.debug("XML is {} is incompatible with Grade Resource", content);

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
    public Response deleteGrade(@PathParam("gradedItemId") int gradedItemId, @PathParam("studentId") int studentId) {
        LOG.info("Deleting the Grade for Graded Item Id= {}, Student Id= {}", gradedItemId, studentId);
        LOG.debug("DELETE request");
        LOG.debug("PathParam gradedItemId = {}", gradedItemId);
        LOG.debug("PathParam studentId = {}", studentId);

        Response response;

        LOG.debug("gradedItemId exists? {}", gradebook.containsGradedItemId(gradedItemId));
        if (!gradebook.containsGradedItemId(gradedItemId)) {
            LOG.info("Creating a {} {} Status Response", Response.Status.PRECONDITION_FAILED.getStatusCode(), Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            LOG.debug("gradedItemId {} does not exist", gradedItemId);
            String message = "Graded Item " + gradedItemId + " does not exist. Cannot remove grade.";
            URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
            response = Response.status(Response.Status.PRECONDITION_FAILED).location(locationURI).entity(message).build();
        } else {
            if (!gradebook.getGradedItemByGradedItemId(gradedItemId).containsStudentId(studentId)) {
                LOG.info("Creating a {} {} Status Response", Response.Status.CONFLICT.getStatusCode(), Response.Status.CONFLICT.getReasonPhrase());
                String message = "Student Id " + String.valueOf(studentId) + " not found";
                response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
            } else {
                LOG.info("Creating a {} {} Status Response", Response.Status.PRECONDITION_FAILED.getStatusCode(), Response.Status.PRECONDITION_FAILED.getReasonPhrase());

                LOG.debug("Removing Grade {}", gradebook.getGradedItemByGradedItemId(gradedItemId).getGradeByStudentId(studentId));
                gradebook.getGradedItemByGradedItemId(gradedItemId).removeGradeByStudentId(studentId);

                LOG.debug("GradedItem {}", gradebook.getGradedItemByGradedItemId(gradedItemId));

                response = Response.status(Response.Status.NO_CONTENT).build();
            }
        }

        LOG.debug("Generated response {}", response);

        return response;
    }

    @PUT
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response updateGrade(@PathParam("gradedItemId") int gradedItemId, @PathParam("studentId") int studentId, String content) {
        LOG.info("Updating the instance Grade {}", grade);
        LOG.debug("PUT request");
        LOG.debug("Request Content = {}", content);

        Response response;

        try {
            LOG.debug("gradedItemId exists? {}", gradebook.containsGradedItemId(gradedItemId));
            if (!gradebook.containsGradedItemId(gradedItemId)) {
                LOG.info("Creating a {} {} Status Response", Response.Status.PRECONDITION_FAILED.getStatusCode(), Response.Status.PRECONDITION_FAILED.getReasonPhrase());
                LOG.debug("gradedItemId {} does not exist", gradedItemId);
                String message = "Graded Item " + gradedItemId + " does not exist.";
                URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                response = Response.status(Response.Status.PRECONDITION_FAILED).location(locationURI).entity(message).build();
            } else {
                if (!gradebook.getGradedItemByGradedItemId(gradedItemId).containsStudentId(studentId)) {
                    LOG.info("Creating a {} {} Status Response", Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
                    String message = "Student Id " + String.valueOf(studentId) + " not found";
                    response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
                } else {
                    LOG.debug("Updating Grade {}", content);
                    gradedItem = (GradedItem) Converter.convertFromXmlToObject(content, GradedItem.class);
                    grade = gradedItem.getGrade().get(0);

                    LOG.debug("Removing old Grade {}", gradedItem.getGradeByStudentId(studentId));
                    gradebook.getGradedItemByGradedItemId(gradedItemId).removeGradeByStudentId(studentId);

                    LOG.debug("Adding new Grade {}", grade);
                    gradebook.getGradedItemByGradedItemId(gradedItemId).addGrade(grade);

                    gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);
                    LOG.debug("GradedItem {}", gradedItem);

                    URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_SELF_VALUE, new GradebookUri(locationURI)));
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_UPDATE_VALUE, new GradebookUri(locationURI)));
                    gradedItem.addLink(new Link(RELATIONS_URI + GRADE_DELETE_VALUE, new GradebookUri(locationURI)));
                    String xmlString = Converter.convertFromObjectToXml(gradedItem, GradedItem.class);

                    LOG.info("Creating a {} {} Status Response", Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase());
                    response = Response.status(Response.Status.OK).entity(xmlString).build();
                    gradedItem.getLink().clear();
                }
            }
        } catch (JAXBException ex) {
            LOG.info("Creating a {} {} Status Response", Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.BAD_REQUEST.getReasonPhrase());
            LOG.debug("XML is {} is incompatible with Grade Resource", content);

            response = Response.status(Response.Status.BAD_REQUEST).entity(content).build();
        } catch (RuntimeException e) {
            LOG.debug("Catch All exception");
            LOG.info("Creating a {} {} Status Response", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(content).build();
        }
        LOG.debug("Generated response {}", response);
        return response;
    }

    @GET
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response getGrade(@PathParam("gradedItemId") int gradedItemId, @PathParam("studentId") int studentId) {
        LOG.info("Getting the instance Grade {}", grade);
        LOG.debug("GET request");

        Response response;

        if (!gradebook.containsGradedItemId(gradedItemId)) {
            LOG.info("Creating a {} {} Status Response", Response.Status.PRECONDITION_FAILED.getStatusCode(), Response.Status.PRECONDITION_FAILED.getReasonPhrase());
            LOG.debug("gradedItemId does not exist", gradedItemId);
            String message = "Graded Item " + gradedItemId + " does not exist.";
            URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
            response = Response.status(Response.Status.PRECONDITION_FAILED).location(locationURI).entity(message).build();
        } else {
            if (!gradebook.getGradedItemByGradedItemId(gradedItemId).containsStudentId(studentId)) {
                LOG.info("Creating a {} {} Status Response", Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
                LOG.debug("studentId does not exist", studentId);

                String message = "Grade of student Id " + String.valueOf(studentId) + " not found";
                response = Response.status(Response.Status.NOT_FOUND).entity(message).build();
            } else {
                LOG.info("Creating a {} {} Status Response", Response.Status.OK.getStatusCode(), Response.Status.OK.getReasonPhrase());
                LOG.debug("Retrieving the Grade for studentId {}", studentId);
                gradedItem = gradebook.getGradedItemByGradedItemId(gradedItemId);
                grade = gradedItem.getGradeByStudentId(studentId);
                GradedItem studentGrade = new GradedItem();
                studentGrade.setGradedItemId(gradedItemId);
                studentGrade.setDescription(gradedItem.getDescription());
                studentGrade.setPercentage(gradedItem.getPercentage());
                studentGrade.addGrade(grade);

                URI locationURI = URI.create(uriInfo.getAbsolutePath().toString());
                studentGrade.addLink(new Link(RELATIONS_URI + GRADE_SELF_VALUE, new GradebookUri(locationURI)));
                studentGrade.addLink(new Link(RELATIONS_URI + GRADE_UPDATE_VALUE, new GradebookUri(locationURI)));
                studentGrade.addLink(new Link(RELATIONS_URI + GRADE_DELETE_VALUE, new GradebookUri(locationURI)));

                String xmlString = Converter.convertFromObjectToXml(studentGrade, GradedItem.class);

                response = Response.status(Response.Status.OK).entity(xmlString).build();
            }

        }

        LOG.debug("Generated response {}", response);

        return response;
    }

}
