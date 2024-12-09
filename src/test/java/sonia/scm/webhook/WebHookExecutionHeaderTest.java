/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.webhook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.Content;
import sonia.scm.net.ahc.RawContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class WebHookExecutionHeaderTest {

  private Supplier<String> getRevertStringExampleSupplier(String revertedString) {
      return () -> {
        char[] revertedCharArray = new char[revertedString.length()];
        for (int i = 0; i < revertedCharArray.length; i++) {
          revertedCharArray[i] = revertedString.charAt(revertedString.length() - 1 - i);
        }

        return new String(revertedCharArray);
      };

  }

  private Function<Content,String> getRevertStringExampleContentTransformer() {
    return c -> {
      try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        c.process(baos);
        byte[] array = baos.toByteArray();
        byte[] revertedArray = new byte[array.length];
        for (int i = 0; i < revertedArray.length; i++) {
          revertedArray[i] = array[array.length - 1 - i];
        }
        return new String(revertedArray, StandardCharsets.UTF_8);
      } catch(IOException e) {
        fail(e.getMessage());
      }
      return null;
    };
  }

  @Test
  void shouldReturnValue() throws WebHookHeaderExecutionException {
    WebHookExecutionHeader header = new WebHookExecutionHeader();
    header.setValue("abc");
    assertEquals("abc", header.getValue());
  }

  @Test
  void shouldProcessValueWithSimpleGivenSupplier() throws WebHookHeaderExecutionException {
    WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", () -> "hello");
    assertEquals("hello", header.getValue());
  }

  @Test
  void shouldProcessValueWithComplexRequestSupplier() throws WebHookHeaderExecutionException {
    WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", getRevertStringExampleSupplier("HelloWorld"));
    String value = header.getValue();
    assertEquals("dlroWolleH", value);
  }

  @Test
  void shouldProcessValueWithComplexRequestFunction() throws WebHookHeaderExecutionException {
    WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", getRevertStringExampleContentTransformer());
    Content content = new RawContent("HelloWorld".getBytes(StandardCharsets.UTF_8));

    String value = header.getValue(content);
    assertEquals("dlroWolleH", value);
  }

  @Test
  void shouldConvertCorrectlyFromWebhookHeaders() throws WebHookHeaderExecutionException {
    WebhookHeader header = new WebhookHeader();
    header.setValue("abc");
    header.setKey("123");

    WebHookExecutionHeader target = WebHookExecutionHeader.from(header);

    assertEquals("abc", target.getValue());
    assertEquals("123", target.getKey());
  }

  @Test
  void shouldConvertCorrectlyFromListOfWebhookHeaders() throws WebHookHeaderExecutionException {
    WebhookHeader header1 = new WebhookHeader();
    header1.setValue("abc");
    header1.setKey("123");

    WebhookHeader header2 = new WebhookHeader();
    header2.setValue("def");
    header2.setKey("456");

    WebhookHeader header3 = new WebhookHeader();
    header3.setValue("ghi");
    header3.setKey("789");

    List<WebhookHeader> headerList = List.of(header1, header2, header3);

    List<WebHookExecutionHeader> targetList = WebHookExecutionHeader.from(headerList);

    assertEquals("123", targetList.get(0).getKey());
    assertEquals("456", targetList.get(1).getKey());
    assertEquals("789", targetList.get(2).getKey());

    assertEquals("abc", targetList.get(0).getValue());
    assertEquals("def", targetList.get(1).getValue());
    assertEquals("ghi", targetList.get(2).getValue());
  }

  @Test
  void shouldThrowExceptionForGetRequestWithContentDependingFunction() {
    WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", c -> {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        c.process(baos);
        fail("I worked. This is something unexpected to happen. Where does the content come from?");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return "";
    });

    assertThrows(WebHookHeaderExecutionException.class, header::getValue);
  }

  @Test
  void shouldThrowExceptionForBrokenContentTransformer() {
    WebHookExecutionHeader header = new WebHookExecutionHeader("someKey", c -> {
      throw new RuntimeException("I should have been caught before.");
    });

    Content exampleContent = new RawContent("SomeText".getBytes(StandardCharsets.UTF_8));

    assertThrows(WebHookHeaderExecutionException.class, () -> header.getValue(exampleContent));
  }
}
