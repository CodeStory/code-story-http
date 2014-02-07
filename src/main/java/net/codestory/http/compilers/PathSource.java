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
package net.codestory.http.compilers;

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.codestory.http.io.*;

import com.github.sommeri.less4j.*;

class PathSource extends LessSource {
  private final Path path;
  private final String content;
  private final String name;

  PathSource(Path path, String content) {
    this.path = path;
    this.content = content;
    String prefix = "/";
    if (path.equals(path.getFileName())) {
      prefix = "";
    }
    this.name = StreamSupport.stream(path.spliterator(), false).map(Path::toString).collect(Collectors.joining("/", prefix, ""));
  }

  @Override
  public LessSource relativeSource(String filename) throws CannotReadFile, FileNotFound {
    Path relativePath = Paths.get(filename);
    if (!Resources.exists(relativePath)) {
      throw new FileNotFound();
    }

    try {
      String includeContent = Resources.read(relativePath, UTF_8);

      return new PathSource(relativePath, includeContent);
    } catch (IOException e) {
      throw new CannotReadFile();
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public byte[] getBytes() throws CannotReadFile, FileNotFound {
    if (!Resources.exists(path)) {
      throw new FileNotFound();
    }

    try {
      return Resources.readBytes(path);
    } catch (IOException e) {
      throw new CannotReadFile();
    }
  }
}
