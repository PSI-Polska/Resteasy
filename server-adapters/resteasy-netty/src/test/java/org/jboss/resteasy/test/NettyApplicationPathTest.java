package org.jboss.resteasy.test;

import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

/**
 * Fetching root path from ApplicationPath annotation related tests.
 *  @see https://issues.jboss.org/browse/RESTEASY-1657
 */
public class NettyApplicationPathTest
{
   private static final String ECHO = "hello";

   @ApplicationPath("/rest-test")
   public static class TestApplication extends Application
   {
      private final Set<Object> singletons = new HashSet<>();

      public TestApplication()
      {
         singletons.add(new EchoService());
      }

      @Override
      public Set<Object> getSingletons()
      {
         return singletons;
      }
   }

   @Path("/")
   public static class EchoService
   {
      @GET
      @Path("/echo")
      @Produces("text/plain")
      public String echo(@QueryParam("text") final String echo)
      {
         return echo;
      }
   }

   @Test
   public void testWithClass() throws Exception
   {
      NettyJaxrsServer server = null;
      Client client = null;
      try
      {
         ResteasyDeployment deployment = new ResteasyDeployment();
         deployment.setApplicationClass(TestApplication.class.getName());
         server = new NettyJaxrsServer();
         server.setDeployment(deployment);
         server.setHostname("localhost");
         server.setPort(8080);
         server.start();

         // call resource
         final String path = "/rest-test/echo";
         client = ClientBuilder.newClient();
         String url = String.format("http://%s:%d%s", server.getHostname(), server.getPort(), path);
         Response response = client.target(url).queryParam("text", ECHO).request().get();
         assertTrue(response.getStatus() == HttpResponseCodes.SC_OK);
         String msg = response.readEntity(String.class);
         assertEquals(ECHO, msg);
      }
      finally
      {
         if (client != null)
         {
            client.close();
         }
         if (server != null)
         {
            server.stop();
         }
      }
   }

   @Test
   public void testWithApplication() throws Exception
   {
      NettyJaxrsServer server = null;
      Client client = null;
      try
      {
         ResteasyDeployment deployment = new ResteasyDeployment();
         Application app = new TestApplication();
         deployment.setApplication(app);
         server = new NettyJaxrsServer();
         server.setDeployment(deployment);
         server.setHostname("localhost");
         server.setPort(8080);
         server.start();

         // call resource
         final String path = "/rest-test/echo";
         client = ClientBuilder.newClient();
         String url = String.format("http://%s:%d%s", server.getHostname(), server.getPort(), path);
         Response response = client.target(url).queryParam("text", ECHO).request().get();
         assertTrue(response.getStatus() == HttpResponseCodes.SC_OK);
         String msg = response.readEntity(String.class);
         assertEquals(ECHO, msg);
      }
      finally
      {
         if (client != null)
         {
            client.close();
         }
         if (server != null)
         {
            server.stop();
         }
      }
   }

   @Test
   public void testWithManualRootPath() throws Exception
   {
      NettyJaxrsServer server = null;
      Client client = null;
      try
      {
         ResteasyDeployment deployment = new ResteasyDeployment();
         deployment.setApplicationClass(TestApplication.class.getName());
         server = new NettyJaxrsServer();
         server.setRootResourcePath("/new-rest-test");
         server.setDeployment(deployment);
         server.setHostname("localhost");
         server.setPort(8080);
         server.start();

         // call resource
         // root resource should be taken from setRootResourcePath method
         final String path = "/new-rest-test/echo";
         client = ClientBuilder.newClient();
         String url = String.format("http://%s:%d%s", server.getHostname(), server.getPort(), path);
         Response response = client.target(url).queryParam("text", ECHO).request().get();
         assertTrue(response.getStatus() == HttpResponseCodes.SC_OK);
         String msg = response.readEntity(String.class);
         assertEquals(ECHO, msg);
      }
      finally
      {
         if (client != null)
         {
            client.close();
         }
         if (server != null)
         {
            server.stop();
         }
      }
   }
}
