package no.cantara.simulator.oauth2stubbedserver;

import no.cantara.commands.config.ConstantValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;

@Path(OAuth2StubbedTokenVerifyResource.OAUTH2TOKENVERIFY_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class OAuth2StubbedTokenVerifyResource {
    public static final String OAUTH2TOKENVERIFY_PATH = "/verify";


    private static final Logger log = LoggerFactory.getLogger(OAuth2StubbedServerResource.class);


    @GET
    public Response getOauth2StubbedServerController(@Context HttpHeaders headers) throws MalformedURLException {

        if (headers != null) {
            log.debug(getClass().getName() + ": headers=" + headers);
            if (!headers.getHeaderString("Authorization").equalsIgnoreCase("Bearer " + ConstantValue.ATOKEN)) {
                log.error("Illegal OAUTH token provided");
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }
        log.trace("pollSystemEvents");
        return Response.status(Response.Status.OK).entity(jSonVerifyResult).build();

    }

    private String jSonVerifyResult = "{\n" +
            "  \"client_id\": \"00000000-0000-4000-a006-000000000001\",\n" +
            "  \"auth_user_id\": \"00000000-0000-4000-a007-000000000001\",\n" +
            "  \"user_id\": \"00000000-0000-4000-a007-000000000001\",\n" +
            "  \"user_type\": \"agent\",\n" +
            "  \"organization_id\": \"\",\n" +
            "  \"tenant_id\": \"\",\n" +
            "  \"customer_id\": \"\",\n" +
            "  \"name\": \"System\",\n" +
            "  \"expires\": 1423832024,\n" +
            "  \"scope\": \"ROLE_PORTAL_ADMIN ROLE_PERMISSION_READER ROLE_PAYMENTMANAGER_USER ROLE_GLOBAL\"\n" +
            "}";

}

