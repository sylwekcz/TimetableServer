
package pl.sylwekczmil.timetableserver.service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.sylwekczmil.timetableserver.Credentials;


@Path("authentication")
public class AuthenticationRESTFacade {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Credentials sampleCred() {
//        TypedQuery<User> q = em.createNamedQuery("User.findAll",User.class);
//        return q.getResultList();
        Credentials c = new Credentials();
        c.setUsername("someusername");
        c.setPassword("somepassword");
        return c;
    }

    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Response authenticateUser(Credentials credentials) {

        String username = credentials.getUsername();
        String password = credentials.getPassword();
        try {

            authenticate(username, password);
            String token = issueToken(username);
            return Response.ok(token).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private void authenticate(String username, String password) throws Exception {
        // Authenticate against a database, LDAP, file or whatever
        // Throw an Exception if the credentials are invalid
    }

    private String issueToken(String username) {

        Random random = new SecureRandom();
        String token = new BigInteger(130, random).toString(32);
        // save to database witch asociated username

        return token;
    }
}
