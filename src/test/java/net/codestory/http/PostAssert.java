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

import static org.hamcrest.Matchers.*;

import com.jayway.restassured.*;
import com.jayway.restassured.specification.*;

class PostAssert {
  private final String path;
  private final ResponseSpecification expect;

  private PostAssert(String path, ResponseSpecification expect) {
    this.path = path;
    this.expect = expect;
  }

  static PostAssert post(String path) {
    return new PostAssert(path, RestAssured.given().port(WebServerTest.server.port()).expect());
  }

  static PostAssert post(String path, String firstParameterName, Object firstParameterValue, Object... parameterNameValuePairs) {
    return new PostAssert(path, RestAssured.given().port(WebServerTest.server.port()).parameters(firstParameterName, firstParameterValue, parameterNameValuePairs).expect());
  }

  static PostAssert post(String path, String body) {
    return new PostAssert(path, RestAssured.given().port(WebServerTest.server.port()).body(body).expect());
  }

  void produces(String content) {
    expect.content(containsString(content)).when().post(path);
  }

  void produces(int statusCode) {
    expect.statusCode(statusCode).when().post(path);
  }
}
