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
package net.codestory.http.io;

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.junit.*;

public class ResourcesTest {
  @Test
  public void exists() {
    assertThat(Resources.exists(Paths.get("index.html"))).isTrue();
    assertThat(Resources.exists(Paths.get("js"))).isFalse();
  }

  @Test
  public void list() {
    assertThat(Resources.list())
        .contains("js/script.coffee", "test.html")
        .doesNotContain("");
  }

  @Test
  public void ordered() {
    Set<String> list = Resources.list();
    Set<String> ordered = new TreeSet<>(list);

    assertThat(list).isEqualTo(ordered);
  }

  @Test
  public void extension() {
    assertThat(Resources.extension(Paths.get("file.txt"))).isEqualTo(".txt");
    assertThat(Resources.extension(Paths.get("file.css.map"))).isEqualTo(".map");
    assertThat(Resources.extension(Paths.get(".dotfile.ext"))).isEqualTo(".ext");

    assertThat(Resources.extension(Paths.get("file"))).isEmpty();
    assertThat(Resources.extension(Paths.get(".dotfile"))).isEmpty();
    assertThat(Resources.extension(Paths.get("."))).isEmpty();
  }

  @Test
  public void read_file_in_unix_format() throws IOException {
    String content = Resources.read(Paths.get("_layouts/layout.html"), UTF_8);

    assertThat(content).doesNotContain("\r");
  }
}
