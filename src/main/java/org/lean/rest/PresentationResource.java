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

    private LeanUtil leanUtil = LeanUtil.getInstance();

    @GET
    @Path("/")
    public Response getPresentations() throws HopException, LeanException {

        List<String> presentationNamesList = leanUtil.getPresentationNames();
        String presentationJson = new Gson().toJson(presentationNamesList);

        return Response.ok().entity(presentationJson).build();
    }

    @GET
    @Path("{presentation}")
    public Response getPresentation(@PathParam("presentation") String presentationName){
        String presentationSvg = "";
        try {
            presentationSvg = leanUtil.getPresentationSVG(presentationName, 0);
        } catch (HopException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (LeanException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return Response.ok().entity(presentationSvg).build();
    }

    @GET
    @Path("{presentation}/{page}/{posXY}")
    public Response getComponentsAt(@PathParam("presentation") String presentationName,
                                    @PathParam("page") int pageNb,
                                    @PathParam("posXY") String posXY ){
        try {
            List<String> componentNames = leanUtil.getComponentsAt(presentationName, pageNb, posXY);
            String componentNamesJson = new Gson().toJson(componentNames);

            return Response.ok().entity(componentNamesJson).build();

        }catch(HopException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }catch(LeanException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
