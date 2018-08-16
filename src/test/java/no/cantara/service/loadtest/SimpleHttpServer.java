package no.cantara.service.loadtest;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SimpleHttpServer {

    Server server;

    void start() {
        server = new Server(0);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
                httpServletResponse.setContentType("application/json;charset=utf-8");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                request.setHandled(true);
                httpServletResponse.getWriter().println("[]");
            }
        });
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int getPort() {
        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        return connector.getLocalPort();
    }

    void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
