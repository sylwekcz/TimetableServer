package pl.sylwekczmil.timetableserver.service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.sylwekczmil.timetableserver.model.Credentials;
import pl.sylwekczmil.timetableserver.model.User;
import pl.sylwekczmil.timetableserver.controller.UserJpaController;

@Path("login")
public class AuthenticationRESTFacade {
    
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

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Credentials sampleCred() {
        
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

            String token =  authenticate(username, password);
            return Response.ok(token).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
   

    private String authenticate(String username, String password) throws Exception {
        
        User u = getJpaController().findUserByUsername(username);
        if (!u.getPassword().equals(password)) {
            throw new Exception();
        }
        
        Random random = new SecureRandom();
        String token = new BigInteger(130, random).toString(32);
        token = username + ":" + token;    
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());  
        c.add(Calendar.DATE, 1);   
        
        u.setToken(token); 
        u.setTokenExpirationDate(c.getTime());        
        getJpaController().edit(u);
        return token;        
        
    }

}
