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
package net.codestory.http.testhelpers;

import net.codestory.http.*;

import org.junit.rules.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class WebServerRule extends ExternalResource {
  private final String previousProdMode = System.getProperty("PROD_MODE");
  private final WebServer server = new WebServer();

  @Override
  protected void before() throws IOException {
    System.setProperty("PROD_MODE", "true");
    cleanCache();


    server.startOnRandomPort();
  }

  private void cleanCache() {
    // Clean cache
    try {
      Files.walkFileTree(Paths.get(System.getProperty("user.home"), ".code-story", "cache"), new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          if (exc == null) {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
          else {
            // directory iteration failed; propagate exception
            throw exc;
          }
        }
      });
    }
    catch (IOException ignore) {

    }
  }

  @Override
  protected void after() {
    server.reset();

    if (previousProdMode == null) {
      System.clearProperty("PROD_MODE");
    } else {
      System.setProperty("PROD_MODE", previousProdMode);
    }
  }

  public WebServer configure(Configuration configuration) {
    return server.configure(configuration);
  }

  public int port() {
    return server.port();
  }
}
