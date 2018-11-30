package webservice;

import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;

@Path("/moccasin")
@RequestScoped
public class MoccasinService implements Serializable {
    private @Inject MoccasinServiceProvider moccasinServiceProvider;

    @Path("/data")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getData(@QueryParam("catalog") String catalog) {
        try {
            ArrayNode result = moccasinServiceProvider.getTreeBuilder().getTree(catalog);
            return Response.ok().entity(result.toString()).build();
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchData(@QueryParam("str") String searchString, @QueryParam("catalog") String catalog) {
        try {
            ArrayNode result = moccasinServiceProvider.getTreeBuilder().searchTree(searchString, catalog);
            return Response.ok().entity(result.toString()).build();
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
