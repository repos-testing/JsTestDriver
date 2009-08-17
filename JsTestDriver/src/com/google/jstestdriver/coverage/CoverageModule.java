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
package com.google.jstestdriver.coverage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.jstestdriver.ResponseStreamFactory;

/**
 * @author corysmith
 *
 */
public class CoverageModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ResponseStreamFactory.class).to(CoverageResponseStreamFactory.class);
    // add handling for when the testOuput directory is not defined.
    bind(CoverageWriter.class).to(LcovWriter.class);
  }
  
  @Named("coverageFileWriter") @Provides @Inject
  public Writer createCoverageFileWriter(@Named("testOutput") String testOut) {
    try {
      File testOutDir = new File(testOut);
      if (!testOutDir.exists()) {
        testOutDir.mkdirs();
      }
      File coverageFile = new File(testOutDir, "coverage.dat");
      if (coverageFile.exists()) {
        coverageFile.delete();
      }
      coverageFile.createNewFile();
      return new FileWriter(coverageFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
