package no.cantara.service.loadtest;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class SimpleHttpServer {

    Server server;

    void start() {
        server = new Server(0);
        server.setHandler(new Handler.Abstract() {
            @Override
            public boolean handle(Request request, Response response, Callback callback) throws Exception {
                String method = request.getMethod();
                if ("PUT".equals(method) || "POST".equals(method)) {
                    Content.Source.asString(request);
                }
                response.getHeaders().put(HttpHeader.CONTENT_TYPE, "application/json;charset=utf-8");
                response.setStatus(HttpStatus.OK_200);
                Content.Sink.write(response, true, "[]" + System.lineSeparator(), callback);
                return true;
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
