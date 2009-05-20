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

import java.util.List;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class JsonCommand {

  public enum CommandType {
    LOADLIBRARIES("loadLibraries"),
    EXECUTE("execute"),
    RUNALLTESTS("runAllTests"),
    RUNTESTS("runTests"),
    LOADTEST("loadTest"),
    RESET("reset"),
    REGISTERCOMMAND("registerCommand");

    private final String command;

    CommandType(String command) {
      this.command = command;
    }

    public String getCommand() {
      return command;
    }
  }

  private final String command;
  private final List<String> parameters;

  public JsonCommand(CommandType command, List<String> parameters) {
    this.command = command.getCommand();
    this.parameters = parameters;
  }
}
