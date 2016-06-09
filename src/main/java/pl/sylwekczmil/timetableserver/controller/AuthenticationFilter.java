
package pl.sylwekczmil.timetableserver.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import javax.annotation.Priority;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import pl.sylwekczmil.timetableserver.Secured;
import pl.sylwekczmil.timetableserver.User;

/*Inject a proxy of the SecurityContext in any REST endpoint class:

    @Context
    SecurityContext securityContext;
The same can be done in a method:

    @GET
    @Secured
    @Path("{id}")
    @Produces("application/json")
    public Response myMethod(@PathParam("id") Long id, 
                             @Context SecurityContext securityContext) {
        ...
    }
And get the Principal:

    Principal principal = securityContext.getUserPrincipal();
    String username = principal.getName();*/



@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private String username;
    
    
     private EntityManagerFactory getEntityManagerFactory() throws NamingException {
        return (EntityManagerFactory) new InitialContext().lookup("java:comp/env/persistence-factory");
    }

    private UserJpaController getJpaController() {
        try {
            UserTransaction utx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            return new UserJpaController(utx, getEntityManagerFactory());
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the HTTP Authorization header from the request
        String authorizationHeader
                = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly 
        if (authorizationHeader == null) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader;

        try {

            // Validate the token
            validateToken(token);

        } catch (Exception e) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }

        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {

                return new Principal() {

                    @Override
                    public String getName() {
                        return username;
                    }
                };
            }

            @Override
            public boolean isUserInRole(String role) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        });

    }

    private void validateToken(String token) throws Exception {
        String[] s = token.split(":");      
        username = s[0];
        User u = getJpaController().findUserByUsername(username);       
        if(!u.getToken().equals(token)&&!u.getTokenExpirationDate().before(new Date())) throw new Exception();               
    }
}
