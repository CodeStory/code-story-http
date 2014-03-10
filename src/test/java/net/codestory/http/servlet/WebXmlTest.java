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
package net.codestory.http.servlet;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.assertion.BodyMatcher;
import com.jayway.restassured.matcher.RestAssuredMatchers;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class WebXmlTest {

  private Server server;

  private int port;

  @Before
  public void startServer() throws Exception {
    port = 8080 + new Random().nextInt(1000);
    server = new Server(port);

    WebAppContext context = new WebAppContext(WebXmlTest.class.getResource("/net/codestory/http/servlet").toExternalForm(), "/");
    context.setParentLoaderPriority(true);

    server.setHandler(context);

    server.start();
  }

  @Test
  public void should_answer_with_webxml() throws Exception {

    RestAssured.port = this.port;
    assertThat(RestAssured.get("/hellowebxml").then()
            .statusCode(200)
            .body(equalTo("Hello World")));

  }

  @After
  public void stopServer() throws Exception {
    server.stop();
  }

}
