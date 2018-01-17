package no.cantara.service;

import no.cantara.service.config.ConfigLoadTestResource;
import no.cantara.service.config.SetupLoadTestResource;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.LoadTestResource;
import no.cantara.service.oauth2ping.PingResource;
import no.cantara.simulator.oauth2stubbedserver.OAuth2StubbedServerResource;
import no.cantara.simulator.oauth2stubbedserver.OAuth2StubbedTokenVerifyResource;
import no.cantara.util.Configuration;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.web.context.ContextLoaderListener;

import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-09
 */
public class Main {
    public static final String CONTEXT_PATH = "/HTTPLoadTest-baseline";
    public static String PORT_NO = "8086";
    public static final String ADMIN_ROLE = "admin";
    public static final String USER_ROLE = "user";

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Integer webappPort;
    private Server server;


    public Main() {
        this.server = new Server();
    }

    public Main withPort(Integer webappPort) {
        this.webappPort = webappPort;
        return this;
    }

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.INFO);

        Integer webappPort = Configuration.getInt("service.port");
        PORT_NO = Integer.toString(webappPort);

        try {

            final Main main = new Main().withPort(webappPort);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    log.debug("ShutdownHook triggered. Exiting loadtest");
                    main.stop();
                }
            });

            main.start();
            log.debug("Finished waiting for Thread.currentThread().join()");
            main.stop();
        } catch (RuntimeException e) {
            log.error("Error during startup. Shutting down ConfigService.", e);
            System.exit(1);
        }
    }

    // https://github.com/psamsotha/jersey-spring-jetty/blob/master/src/main/java/com/underdog/jersey/spring/jetty/JettyServerMain.java
    public void start() {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(CONTEXT_PATH);


        ConstraintSecurityHandler securityHandler = buildSecurityHandler();
        context.setSecurityHandler(securityHandler);

        ResourceConfig jerseyResourceConfig = new ResourceConfig();
        jerseyResourceConfig.packages("no.cantara");
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(jerseyResourceConfig));
        context.addServlet(jerseyServlet, "/*");

        context.addEventListener(new ContextLoaderListener());

        context.setInitParameter("contextConfigLocation", "classpath:context.xml");

        ServerConnector connector = new ServerConnector(server);
        if (webappPort != null) {
            connector.setPort(webappPort);
        }
        NCSARequestLog requestLog = buildRequestLog();
        server.setRequestLog(requestLog);
        server.addConnector(connector);
        server.setHandler(context);

        try {
            server.start();
        } catch (Exception e) {
            log.error("Error during Jetty startup. Exiting", e);
            // "System. exit(2);"
        }
        webappPort = connector.getLocalPort();
        log.info("HTTPLoadTest-baseline started on http://localhost:{}{}", webappPort, CONTEXT_PATH);
        log.info("Starting, use ./config_override/application.properties to override default properties");

        try {
            server.join();
        } catch (InterruptedException e) {
            log.error("Jetty server thread when join. Pretend everything is OK.", e);
        }
    }

    private NCSARequestLog buildRequestLog() {
        NCSARequestLog requestLog = new NCSARequestLog("logs/jetty-yyyy_mm_dd.request.log");
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogTimeZone("GMT");

        return requestLog;
    }

    private ConstraintSecurityHandler buildSecurityHandler() {
        Constraint userRoleConstraint = new Constraint();
        userRoleConstraint.setName(Constraint.__BASIC_AUTH);
        userRoleConstraint.setRoles(new String[]{USER_ROLE, ADMIN_ROLE});
        userRoleConstraint.setAuthenticate(true);

        Constraint adminRoleConstraint = new Constraint();
        adminRoleConstraint.setName(Constraint.__BASIC_AUTH);
        adminRoleConstraint.setRoles(new String[]{ADMIN_ROLE});
        adminRoleConstraint.setAuthenticate(true);

        ConstraintMapping clientConstraintMapping = new ConstraintMapping();
        clientConstraintMapping.setConstraint(userRoleConstraint);
        clientConstraintMapping.setPathSpec("/client/*");

        ConstraintMapping adminRoleConstraintMapping = new ConstraintMapping();
        adminRoleConstraintMapping.setConstraint(adminRoleConstraint);
        adminRoleConstraintMapping.setPathSpec("/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(clientConstraintMapping);
        securityHandler.addConstraintMapping(adminRoleConstraintMapping);


        // Allow healthresource to be accessed without authentication
        ConstraintMapping healthEndpointConstraintMapping = new ConstraintMapping();
        healthEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
        healthEndpointConstraintMapping.setPathSpec(HealthResource.HEALTH_PATH);
        securityHandler.addConstraintMapping(healthEndpointConstraintMapping);

        Boolean loadtest_basicauth = Configuration.getBoolean("loadtest.basicauth");
        if (!loadtest_basicauth) {
            // Allow configresource to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configEndpointConstraintMapping = new ConstraintMapping();
            configEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            configEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH);
            securityHandler.addConstraintMapping(configEndpointConstraintMapping);

            // Allow configresource to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configEndpoint2ConstraintMapping = new ConstraintMapping();
            configEndpoint2ConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            configEndpoint2ConstraintMapping.setPathSpec(SetupLoadTestResource.SETUP_PATH);
            securityHandler.addConstraintMapping(configEndpoint2ConstraintMapping);

            // Allow configresource read-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configReadEndpointConstraintMapping = new ConstraintMapping();
            configReadEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            configReadEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_READ);
            securityHandler.addConstraintMapping(configReadEndpointConstraintMapping);

            // Allow configresource write-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configWriteEndpointConstraintMapping = new ConstraintMapping();
            configWriteEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            configWriteEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_WRITE);
            securityHandler.addConstraintMapping(configWriteEndpointConstraintMapping);

            // Allow configresource write-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configSelectEndpointConstraintMapping = new ConstraintMapping();
            configSelectEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            configSelectEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_SELECT_TESTSPECIFICATIONSET);
            securityHandler.addConstraintMapping(configSelectEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestEndpointConstraintMapping = new ConstraintMapping();
            loadTestEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH);
            securityHandler.addConstraintMapping(loadTestEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadTestFormEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM);
            securityHandler.addConstraintMapping(loadTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadReadTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadReadTestFormEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadReadTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_READ);
            securityHandler.addConstraintMapping(loadReadTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadWriteTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadWriteTestFormEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadWriteTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_WRITE);
            securityHandler.addConstraintMapping(loadWriteTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadSelectTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadSelectTestFormEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadSelectTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_SELECT);
            securityHandler.addConstraintMapping(loadSelectTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestRunStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestRunStatusEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestRunStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_STATUS);
            securityHandler.addConstraintMapping(loadTestRunStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestStatusEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_RUNSTATUS);
            securityHandler.addConstraintMapping(loadTestStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFullStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestFullStatusEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestFullStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FULLSTATUS);
            securityHandler.addConstraintMapping(loadTestFullStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFullStatusCSVEndpointConstraintMapping = new ConstraintMapping();
            loadTestFullStatusCSVEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestFullStatusCSVEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FULLSTATUS_CSV);
            securityHandler.addConstraintMapping(loadTestFullStatusCSVEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestStopEndpointConstraintMapping = new ConstraintMapping();
            loadTestStopEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
            loadTestStopEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_STOP);
            securityHandler.addConstraintMapping(loadTestStopEndpointConstraintMapping);

        }


        // Allow OAuth2StubbedServerResource to be accessed without authentication
        ConstraintMapping oauthserverEndpointConstraintMapping = new ConstraintMapping();
        oauthserverEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
        oauthserverEndpointConstraintMapping.setPathSpec(OAuth2StubbedServerResource.OAUTH2TOKENSERVER_PATH);
        securityHandler.addConstraintMapping(oauthserverEndpointConstraintMapping);

        // Allow OAuth2StubbedServerResource to be accessed without authentication
        ConstraintMapping pingEndpointConstraintMapping = new ConstraintMapping();
        pingEndpointConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
        pingEndpointConstraintMapping.setPathSpec(PingResource.PING_PATH);
        securityHandler.addConstraintMapping(pingEndpointConstraintMapping);


        // Allow tokenverifyerResource to be accessed without authentication
        ConstraintMapping tokenVerifyConstraintMapping = new ConstraintMapping();
        tokenVerifyConstraintMapping.setConstraint(new Constraint(Constraint.NONE, Constraint.ANY_ROLE));
        tokenVerifyConstraintMapping.setPathSpec(OAuth2StubbedTokenVerifyResource.OAUTH2TOKENVERIFY_PATH);
        securityHandler.addConstraintMapping(tokenVerifyConstraintMapping);

        HashLoginService loginService = new HashLoginService("HTTPLoadTest-baseline");

        String clientUsername = Configuration.getString("login.user");
        String clientPassword = Configuration.getString("login.password");
        UserStore userStore = new UserStore();
        userStore.addUser(clientUsername, new Password(clientPassword), new String[]{USER_ROLE});

        String adminUsername = Configuration.getString("login.admin.user");
        String adminPassword = Configuration.getString("login.admin.password");
        userStore.addUser(adminUsername, new Password(adminPassword), new String[]{ADMIN_ROLE});
        loginService.setUserStore(userStore);

        log.debug("Main instantiated with basic auth clientuser={} and adminuser={}", clientUsername, adminUsername);
        securityHandler.setLoginService(loginService);
        return securityHandler;
    }


    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.warn("Error when stopping Jetty server", e);
        }
    }

    public int getPort() {
        return webappPort;
    }

    public boolean isStarted() {
        return server.isStarted();
    }
}