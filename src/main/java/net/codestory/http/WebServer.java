/**
 * Copyright (C) 2013 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;

import net.codestory.http.errors.*;
import net.codestory.http.filters.log.*;
import net.codestory.http.internal.*;
import net.codestory.http.misc.*;
import net.codestory.http.payload.*;
import net.codestory.http.reload.*;
import net.codestory.http.routes.*;
import net.codestory.http.ssl.*;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.*;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebServer implements Filter {
  private final static Logger LOG = LoggerFactory.getLogger(WebServer.class);

  private Server server;
  private RoutesProvider routesProvider;
  private int port;

  public WebServer() {
    this(routes -> {
    });
  }

  public WebServer(Configuration configuration) {
    configure(configuration);
  }

  public static void main(String[] args) throws Exception {
    new WebServer(routes -> routes
        .filter(new LogRequestFilter()))
        .start(8080);
  }

  public WebServer configure(Configuration configuration) {
    routesProvider = Env.INSTANCE.prodMode()
        ? RoutesProvider.fixed(configuration)
        : RoutesProvider.reloading(configuration);
    return this;
  }

  public WebServer startOnRandomPort() {
    Random random = new Random();
    for (int i = 0; i < 20; i++) {
      try {
        int port = 8183 + random.nextInt(1000);
        start(port);
        return this;
      } catch (Exception e) {
        LOG.error("Unable to bind server", e);
      }
    }
    throw new IllegalStateException("Unable to start server");
  }

  public WebServer start() {
    return start(8080);
  }

  public WebServer start(int port) {
    return startWithContext(port, null);
  }

  public WebServer startSSL(int port, Path pathCertificate, Path pathPrivateKey) {
    SslContextFactory context;
    try {
      context = new SSLContextFactory().create(pathCertificate, pathPrivateKey);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to read certificate or key", e);
    }
    return startWithContext(port, context);
  }

  private WebServer startWithContext(int port, SslContextFactory context) {
    try {
      this.port = Env.INSTANCE.overriddenPort(port);

      if (context == null) {
        server = new Server(this.port);
      } else {
        server = new Server();

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(context, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(this.port);

        server.addConnector(sslConnector);
      }

      ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
      servletHandler.addFilter(new FilterHolder(this), "/*", EnumSet.of(DispatcherType.REQUEST));

      server.setHandler(servletHandler);

      server.start();

      LOG.info("Server started on port {}", this.port);
    } catch (RuntimeException e) {
      throw e;
    } catch (BindException e) {
      throw new IllegalStateException("Port already in use " + this.port);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to bind the web server on port " + this.port, e);
    }

    return this;
  }

  public int port() {
    return port;
  }

  public void reset() {
    configure(routes -> {
    });
  }

  public void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to stop the web server", e);
    }
  }

  protected void applyRoutes(RouteCollection routeCollection, Context context) throws IOException {
    Payload payload = routeCollection.apply(context);
    if (payload.isError()) {
      payload = errorPage(payload);
    }
    payload.writeTo(context);
  }

  protected void handleServerError(Context context, Exception e) {
    if (!(e instanceof HttpException)) {
      e.printStackTrace();
    }

    try {
      errorPage(e).writeTo(context);
    } catch (IOException error) {
      LOG.warn("Unable to serve an error page", error);
    }
  }

  protected Payload errorPage(Payload payload) {
    return errorPage(payload, null);
  }

  protected Payload errorPage(Exception e) {
    int code = (e instanceof HttpException) ? ((HttpException) e).code() : 500;
    return errorPage(new Payload(code), e);
  }

  protected Payload errorPage(Payload payload, Exception e) {
    Exception shownError = Env.INSTANCE.prodMode() ? null : e;
    return new ErrorPage(payload, shownError).payload();
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // TODO, call spec class no init routes of user.
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    Context context = null;

    try {
      RouteCollection routes = routesProvider.get();
      context = new Context((HttpServletRequest)request, (HttpServletResponse)response, routes.getIocAdapter());

      applyRoutes(routes, context);
    } catch (Exception e) {
      if (context == null) {
        // Didn't manage to initialize a full context
        // because the routes failed to load
        //
        context = new Context((HttpServletRequest)request, (HttpServletResponse)response, null);
      }
      handleServerError(context, e);
    } finally {
      try {
        response.getOutputStream().close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  @Override
  public void destroy() {

  }
}
