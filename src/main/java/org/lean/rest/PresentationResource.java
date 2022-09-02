package org.lean.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.hop.core.exception.HopException;
import org.lean.core.exception.LeanException;

import java.util.List;

@Path("presentations")
public class PresentationResource {

    @GET
    @Path("/")
    public Response getPresentations(){
        List<String> presentationNamesList = LeanUtil.getInstance().getPresentationNames();
        String presentationJson = new Gson().toJson(presentationNamesList);

        return Response.ok().entity(presentationJson).build();
    }

    @GET
    @Path("{presentation}")
    public Response getPresentation(@PathParam("presentation") String presentationName){
        String presentationSvg = "";
        try {
            presentationSvg = LeanUtil.getInstance().getPresentationSVG(presentationName, 0);
        } catch (HopException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (LeanException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return Response.ok().entity(presentationSvg).build();
    }
}
