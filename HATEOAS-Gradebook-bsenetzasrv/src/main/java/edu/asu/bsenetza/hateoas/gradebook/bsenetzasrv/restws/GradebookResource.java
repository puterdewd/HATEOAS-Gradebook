package edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.restws;

import edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.GradebookManager;
import static edu.asu.bsenetza.hateoas.gradebook.bsenetzasrv.representation.Representation.*;
import java.lang.invoke.MethodHandles;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bsenetza
 */
@Path("gradebook")
public class GradebookResource {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GradebookManager gradebook = GradebookManager.getInstance();

    @Context UriInfo uriInfo;

    /**
     * Creates a new instance of GradebookResource
     */
    public GradebookResource() {
        LOG.info("Creating an Gradebook Resource");
    }

    @POST
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response createGradebook(String content) {
        LOG.info("Creating the instance Gradebook {}", gradebook);
        LOG.debug("POST request");
        LOG.debug("Request Content = {}", content);

        Response response;
        LOG.info("Creating a gradebook is not yet implemented", gradebook);
        response = Response.status(Response.Status.NOT_IMPLEMENTED).entity("Creating a gradebook is not yet implemented").build();
        LOG.debug("Generated response {}", response);

        return response;
    }

    @DELETE
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response deleteGradebook(String content) {
        LOG.info("Deleting the instance Gradebook {}", gradebook);
        LOG.debug("DELETE request");

        Response response;
        LOG.info("Deleting a gradebook is not yet implemented", gradebook);
        response = Response.status(Response.Status.NOT_IMPLEMENTED).entity("Deleting a gradebook is not yet implemented").build();
        LOG.debug("Generated response {}", response);

        return response;
    }

    @PUT
    @Consumes(GRADEBOOK_MEDIA_TYPE)
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response updateGradebook(String content) {
        LOG.info("Updating the instance Gradebook {}", gradebook);
        LOG.debug("PUT request");
        LOG.debug("Request Content = {}", content);

        Response response;
        LOG.info("Updating a gradebook is not yet implemented", gradebook);
        response = Response.status(Response.Status.NOT_IMPLEMENTED).entity("Updating a gradebook is not yet implemented").build();
        LOG.debug("Generated response {}", response);

        return response;
    }

    @GET
    @Produces(GRADEBOOK_MEDIA_TYPE)
    public Response getGradebook() {
        LOG.info("Getting the instance Gradebook {}", gradebook);
        LOG.debug("GET request");

        Response response;
        LOG.info("Getting a gradebook is not yet implemented", gradebook);
        response = Response.status(Response.Status.NOT_IMPLEMENTED).entity("Getting a gradebook is not yet implemented").build();
        LOG.debug("Generated response {}", response);

        return response;
    }

}
