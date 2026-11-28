package no.cantara.service;

import no.cantara.service.config.ConfigLoadTestResource;
import no.cantara.service.config.SetupLoadTestResource;
import no.cantara.service.health.HealthResource;
import no.cantara.service.loadtest.LoadTestResource;
import no.cantara.service.oauth2ping.PingResource;
import no.cantara.simulator.RestTestResource;
import no.cantara.simulator.oauth2stubbedserver.OAuth2StubbedServerResource;
import no.cantara.simulator.oauth2stubbedserver.OAuth2StubbedTokenVerifyResource;
import no.cantara.util.Configuration;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.web.context.ContextLoaderListener;

import java.io.File;
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
        jerseyResourceConfig.register(MultiPartFeature.class);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(jerseyResourceConfig));
        context.addServlet(jerseyServlet, "/*");

        context.addEventListener(new ContextLoaderListener());

        context.setInitParameter("contextConfigLocation", "classpath:context.xml");

        ServerConnector connector = new ServerConnector(server);
        if (webappPort != null) {
            connector.setPort(webappPort);
        }
        CustomRequestLog requestLog = buildRequestLog();
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

    private CustomRequestLog buildRequestLog() {
        // Jetty 10 removed NCSARequestLog; this is its documented replacement.
        RequestLogWriter writer = new RequestLogWriter("logs/jetty-yyyy_mm_dd.request.log");
        writer.setAppend(true);
        writer.setTimeZone("GMT");
        return new CustomRequestLog(writer, CustomRequestLog.EXTENDED_NCSA_FORMAT);
    }

    private ConstraintSecurityHandler buildSecurityHandler() {
        Constraint userRoleConstraint = Constraint.from(USER_ROLE, ADMIN_ROLE);

        Constraint adminRoleConstraint = Constraint.from(ADMIN_ROLE);

        ConstraintMapping clientConstraintMapping = new ConstraintMapping();
        clientConstraintMapping.setConstraint(userRoleConstraint);
        clientConstraintMapping.setPathSpec("/client/*");

        // Allow testresource to be accessed without authentication
        ConstraintMapping testEndpointConstraintMapping = new ConstraintMapping();
        testEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
        testEndpointConstraintMapping.setPathSpec("/test/*");

        ConstraintMapping adminRoleConstraintMapping = new ConstraintMapping();
        adminRoleConstraintMapping.setConstraint(adminRoleConstraint);
        adminRoleConstraintMapping.setPathSpec("/*");

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.addConstraintMapping(clientConstraintMapping);
        securityHandler.addConstraintMapping(testEndpointConstraintMapping);
        securityHandler.addConstraintMapping(adminRoleConstraintMapping);


        // Allow healthresource to be accessed without authentication
        ConstraintMapping healthEndpointConstraintMapping = new ConstraintMapping();
        healthEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
        healthEndpointConstraintMapping.setPathSpec(HealthResource.HEALTH_PATH);
        securityHandler.addConstraintMapping(healthEndpointConstraintMapping);


        Boolean loadtest_basicauth = Configuration.getBoolean("loadtest.basicauth");
        if (!loadtest_basicauth) {
            // Allow configresource to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configEndpointConstraintMapping = new ConstraintMapping();
            configEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH);
            securityHandler.addConstraintMapping(configEndpointConstraintMapping);

            // Allow configresource to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configTraceEndpointConstraintMapping = new ConstraintMapping();
            configTraceEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configTraceEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_TRACE);
            securityHandler.addConstraintMapping(configTraceEndpointConstraintMapping);

            // Allow configresource to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configEndpoint2ConstraintMapping = new ConstraintMapping();
            configEndpoint2ConstraintMapping.setConstraint(Constraint.ALLOWED);
            configEndpoint2ConstraintMapping.setPathSpec(SetupLoadTestResource.SETUP_PATH);
            securityHandler.addConstraintMapping(configEndpoint2ConstraintMapping);

            // Allow configresource read-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configLoadEndpointConstraintMapping = new ConstraintMapping();
            configLoadEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configLoadEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_LOAD);
            securityHandler.addConstraintMapping(configLoadEndpointConstraintMapping);

            // Allow configresource read-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configReadEndpointConstraintMapping = new ConstraintMapping();
            configReadEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configReadEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_READ);
            securityHandler.addConstraintMapping(configReadEndpointConstraintMapping);

            // Allow configresource write-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configWriteEndpointConstraintMapping = new ConstraintMapping();
            configWriteEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configWriteEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_WRITE);
            securityHandler.addConstraintMapping(configWriteEndpointConstraintMapping);

            // Allow configresource write-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configBenchmarkEndpointConstraintMapping = new ConstraintMapping();
            configBenchmarkEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configBenchmarkEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_BENCHMARK);
            securityHandler.addConstraintMapping(configBenchmarkEndpointConstraintMapping);

            // Allow configresource write-test_setup to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping configSelectEndpointConstraintMapping = new ConstraintMapping();
            configSelectEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            configSelectEndpointConstraintMapping.setPathSpec(ConfigLoadTestResource.CONFIG_PATH_SELECT_TESTSPECIFICATIONSET);
            securityHandler.addConstraintMapping(configSelectEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestEndpointConstraintMapping = new ConstraintMapping();
            loadTestEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH);
            securityHandler.addConstraintMapping(loadTestEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestTraceEndpointConstraintMapping = new ConstraintMapping();
            loadTestTraceEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestTraceEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_TRACE);
            securityHandler.addConstraintMapping(loadTestTraceEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestZipEndpointConstraintMapping = new ConstraintMapping();
            loadTestZipEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestZipEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_ZIP);
            securityHandler.addConstraintMapping(loadTestZipEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadTestFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM);
            securityHandler.addConstraintMapping(loadTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestLoadFormEndpointConstraintMapping = new ConstraintMapping();
            loadTestLoadFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestLoadFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_LOAD);
            securityHandler.addConstraintMapping(loadTestLoadFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestLoadTraceFormEndpointConstraintMapping = new ConstraintMapping();
            loadTestLoadTraceFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestLoadTraceFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_LOAD_TRACE);
            securityHandler.addConstraintMapping(loadTestLoadTraceFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadReadTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadReadTestFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadReadTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_READ);
            securityHandler.addConstraintMapping(loadReadTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadWriteTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadWriteTestFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadWriteTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_WRITE);
            securityHandler.addConstraintMapping(loadWriteTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadBenchmarkFormEndpointConstraintMapping = new ConstraintMapping();
            loadBenchmarkFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadBenchmarkFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_BENCHMARK);
            securityHandler.addConstraintMapping(loadBenchmarkFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadSelectTestFormEndpointConstraintMapping = new ConstraintMapping();
            loadSelectTestFormEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadSelectTestFormEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FORM_SELECT);
            securityHandler.addConstraintMapping(loadSelectTestFormEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestRunStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestRunStatusEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestRunStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_STATUS);
            securityHandler.addConstraintMapping(loadTestRunStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestDashboardEndpointConstraintMapping = new ConstraintMapping();
            loadTestDashboardEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestDashboardEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_DASHBOARD);
            securityHandler.addConstraintMapping(loadTestDashboardEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestStatusEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_RUNSTATUS);
            securityHandler.addConstraintMapping(loadTestStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFullStatusEndpointConstraintMapping = new ConstraintMapping();
            loadTestFullStatusEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestFullStatusEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FULLSTATUS);
            securityHandler.addConstraintMapping(loadTestFullStatusEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestFullStatusCSVEndpointConstraintMapping = new ConstraintMapping();
            loadTestFullStatusCSVEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestFullStatusCSVEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_FULLSTATUS_CSV);
            securityHandler.addConstraintMapping(loadTestFullStatusCSVEndpointConstraintMapping);

            // Allow loadTest to be accessed without authentication   (for now, should be protected for pipeline CD/CP use))
            ConstraintMapping loadTestStopEndpointConstraintMapping = new ConstraintMapping();
            loadTestStopEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
            loadTestStopEndpointConstraintMapping.setPathSpec(LoadTestResource.APPLICATION_PATH_STOP);
            securityHandler.addConstraintMapping(loadTestStopEndpointConstraintMapping);

        }


        // Allow OAuth2StubbedServerResource to be accessed without authentication
        ConstraintMapping oauthserverEndpointConstraintMapping = new ConstraintMapping();
        oauthserverEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
        oauthserverEndpointConstraintMapping.setPathSpec(OAuth2StubbedServerResource.OAUTH2TOKENSERVER_PATH);
        securityHandler.addConstraintMapping(oauthserverEndpointConstraintMapping);

        // Allow OAuth2StubbedServerResource to be accessed without authentication
        ConstraintMapping pingEndpointConstraintMapping = new ConstraintMapping();
        pingEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
        pingEndpointConstraintMapping.setPathSpec(PingResource.PING_PATH);
        securityHandler.addConstraintMapping(pingEndpointConstraintMapping);

        // Allow RestTestResource to be accessed without authentication
        ConstraintMapping restTestEndpointConstraintMapping = new ConstraintMapping();
        restTestEndpointConstraintMapping.setConstraint(Constraint.ALLOWED);
        restTestEndpointConstraintMapping.setPathSpec(RestTestResource.REST_PATH + "/*");
        securityHandler.addConstraintMapping(restTestEndpointConstraintMapping);

        // Allow tokenverifyerResource to be accessed without authentication
        ConstraintMapping tokenVerifyConstraintMapping = new ConstraintMapping();
        tokenVerifyConstraintMapping.setConstraint(Constraint.ALLOWED);
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

        // Bootstrapping
        File debugFile = new File(System.getProperty("user.dir") + "/logs/debug_file.log");
        debugFile = null;

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