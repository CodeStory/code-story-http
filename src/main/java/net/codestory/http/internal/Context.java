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
package net.codestory.http.internal;

import static net.codestory.http.constants.Headers.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.codestory.http.convert.*;
import net.codestory.http.injection.*;
import net.codestory.http.io.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Context {
  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final IocAdapter iocAdapter;
  private String currentUser;

  public Context(HttpServletRequest request, HttpServletResponse response, IocAdapter iocAdapter) {
    this.request = request;
    this.response = response;
    this.iocAdapter = iocAdapter;
  }

  public String uri() {
    return request.getPathInfo();
  }

  public Cookie cookie(String name) {
    return Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName())).findFirst().orElse(null);
  }

  public String cookieValue(String name) {
    Cookie cookie = cookie(name);
    return (cookie == null) ? null : cookie.getValue();
  }

  @SuppressWarnings("unchecked")
  public <T> T cookieValue(String name, T defaultValue) {
    T value = cookieValue(name, (Class<T>) defaultValue.getClass());
    return (value == null) ? defaultValue : value;
  }

  @SuppressWarnings("unchecked")
  public <T> T cookieValue(String name, Class<T> type) {
    String value = cookieValue(name);
    return (value == null) ? null : TypeConvert.fromJson(value, type);
  }

  public String cookieValue(String name, String defaultValue) {
    String value = cookieValue(name);
    return (value == null) ? defaultValue : value;
  }

  public int cookieValue(String name, int defaultValue) {
    String value = cookieValue(name);
    return (value == null) ? defaultValue : Integer.parseInt(value);
  }

  public long cookieValue(String name, long defaultValue) {
    String value = cookieValue(name);
    return (value == null) ? defaultValue : Long.parseLong(value);
  }

  public boolean cookieValue(String name, boolean defaultValue) {
    String value = cookieValue(name);
    return (value == null) ? defaultValue : Boolean.parseBoolean(value);
  }

  public List<Cookie> cookies() {
    return Arrays.asList(request.getCookies());
  }

  public String get(String name) {
    return request.getParameter(name);
  }

  public List<String> getAll(String name) {
    return Arrays.asList(request.getParameterValues(name));
  }

  public int getInteger(String name) {
    return Integer.parseInt(request.getParameter(name));
  }

  public float getFloat(String name) {
    return Float.parseFloat(request.getParameter(name));
  }

  public boolean getBoolean(String name) {
    return Boolean.parseBoolean(request.getParameter(name));
  }

  public String getHeader(String name) {
    return request.getHeader(name);
  }

  public List<String> getHeaders(String name) {
    return Collections.list(request.getHeaders(name));
  }

  public String method() {
    return request.getMethod();
  }

  public Map<String, String> keyValues() {
    return Collections.list(request.getParameterNames()).stream().collect(Collectors.toMap(
            Function.identity(),
            name -> request.getParameter(name)
    ));
  }

  public String getClientAddress() {
    String forwarded = getHeader(X_FORWARDED_FOR);
    return (forwarded != null) ? forwarded : request.getRemoteAddr();
  }

  public HttpServletRequest request() {
    return request;
  }

  public HttpServletResponse response() {
    return response;
  }

  public byte[] payload() {
    try {
      return InputStreams.readBytes(request.getInputStream());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read payload", e);
    }
  }

  public <T> T payload(Class<T> type) {
    return TypeConvert.convert(this, type);
  }

  public <T> T getBean(Class<T> type) {
    return iocAdapter.get(type);
  }

  public void setCurrentUser(String user) {
    this.currentUser = user;
  }

  public String currentUser() {
    return currentUser;
  }
}
