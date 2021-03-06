/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver;

import com.google.inject.Inject;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.RunData;

import java.io.PrintStream;


/**
 * Resets the browser to the original capture state.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class ResetAction implements BrowserAction {

  public static class ResetActionResponseStream implements ResponseStream {
    
    private final PrintStream out;
    
    public ResetActionResponseStream(PrintStream out) {
      this.out = out;
    }
    
    public void finish() {
    }
    
    public void stream(Response response) {
      out.println(String.format("%s: %s", response.getBrowser().getName(),
          response.getResponse()));
    }
  }
  private final ResponseStreamFactory responseStreamFactory;


  @Inject
  public ResetAction(ResponseStreamFactory responseStreamFactory) {
    this.responseStreamFactory = responseStreamFactory;
  }

  public ResponseStream run(String id, JsTestDriverClient client, RunData runData, JstdTestCase testCase) {
    final ResponseStream responseStream = responseStreamFactory.getResetActionResponseStream();
    client.reset(id, responseStream, testCase);
    return responseStream;
  }
}
