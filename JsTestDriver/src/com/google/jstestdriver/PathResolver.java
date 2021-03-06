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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.hooks.FileParsePostProcessor;

import org.apache.oro.io.GlobFilenameFilter;
import org.apache.oro.text.GlobCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Handles the resolution of glob paths (*.js) and relative paths.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class PathResolver {

  private final Set<FileParsePostProcessor> processors;
  private final File basePath;

  @Inject
  public PathResolver(@Named("basePath") File basePath, Set<FileParsePostProcessor> processors) {
    this.basePath = basePath;
    this.processors = processors;
  }

  // TODO(andrewtrenk): This method may not be needed since the File class
  // can resolve ".." on its own
  public String resolvePath(String path) {
    path  = FileInfo.formatFileSeparator(path);
    Stack<String> resolvedPath = new Stack<String>();
    String[] tokenizedPath = path.split(FileInfo.SEPARATOR_CHAR);

    for (String token : tokenizedPath) {
      if (token.equals("..")) {
        if (!resolvedPath.isEmpty()) {
          resolvedPath.pop();
          continue;
        }
      }
      resolvedPath.push(token);
    }
    return join(resolvedPath);
  }

  private String join(Collection<String> collection) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> iterator = collection.iterator();

    if (iterator.hasNext()) {
      sb.append(iterator.next());

      while (iterator.hasNext()) {
        sb.append(FileInfo.SEPARATOR_CHAR);
        sb.append(iterator.next());
      }
    }
    return sb.toString();
  }

  private Set<FileInfo> consolidatePatches(Set<FileInfo> resolvedFilesLoad) {
    Set<FileInfo> consolidated = new LinkedHashSet<FileInfo>(resolvedFilesLoad.size());
    FileInfo currentNonPatch = null;
    for (FileInfo fileInfo : resolvedFilesLoad) {
      if (fileInfo.isPatch()) {
        if (currentNonPatch == null) {
          throw new IllegalStateException("Patch " + fileInfo
            + " without a core file to patch");
        }
        currentNonPatch.addPatch(fileInfo);
      } else {
        consolidated.add(fileInfo);
        currentNonPatch = fileInfo;
      }
    }
    return consolidated;
  }

  
  /**
   * Resolves files for a set of FileInfos:
   *  - Expands glob paths (e.g. "*.js") into distinct FileInfos
   *  - Sets last modified timestamp for each FileInfo
   *
   * @param unresolvedFiles the FileInfos to resolved
   * @return the resolved FileInfos
   */
  public Set<FileInfo> resolve(Set<FileInfo> unresolvedFiles) {
    Set<FileInfo> resolvedFiles = new LinkedHashSet<FileInfo>();

    for (FileInfo fileInfo : unresolvedFiles) {
      String filePath = fileInfo.getFilePath();

      if (fileInfo.isWebAddress()) {
        resolvedFiles.add(fileInfo.fromResolvedPath(filePath, -1));
      } else {
        File file = basePath != null
            ? new File(basePath.getAbsoluteFile(), filePath)
            : new File(filePath);
        File absoluteDir = file.getParentFile().getAbsoluteFile();
        File relativeDir = new File(filePath).getParentFile();

        // Get all files for the current FileInfo. This will return one file if the FileInfo
        // doesn't represent a glob
        String[] expandedFileNames = expandGlob(absoluteDir.getAbsolutePath(), file.getName(), absoluteDir);

        for (String fileName : expandedFileNames) {
          String absoluteResolvedFilePath = FileInfo.getPath(absoluteDir, fileName);
          String relativeResolvedFilePath = FileInfo.getPath(relativeDir, fileName);

          File resolvedFile = new File(absoluteResolvedFilePath);
          long timestamp = resolvedFile.lastModified();
          FileInfo newFileInfo = fileInfo.fromResolvedPath(relativeResolvedFilePath, timestamp);

          resolvedFiles.add(newFileInfo);
        }
      }
    }

    resolvedFiles = postProcessFiles(resolvedFiles);

    return consolidatePatches(resolvedFiles);
  }

  private String[] expandGlob(String filePath, String fileNamePattern, File dir) {
    String[] filteredFiles = dir.list(new GlobFilenameFilter(
        fileNamePattern, GlobCompiler.DEFAULT_MASK | GlobCompiler.CASE_INSENSITIVE_MASK));

    if (filteredFiles == null || filteredFiles.length == 0) {
      try {
        String error = "The patterns/paths "
          + filePath + " (" + dir + ") "
          + " used in the configuration"
          + " file didn't match any file, the files patterns/paths need to"
          + " be relative " + basePath.getCanonicalPath();
        throw new IllegalArgumentException(error);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    Arrays.sort(filteredFiles, String.CASE_INSENSITIVE_ORDER);

    return filteredFiles;
  }

  public List<Plugin> resolve(List<Plugin> plugins) {
    List<Plugin> resolved = Lists.newLinkedList();
    for (Plugin plugin : plugins) {
      resolved.add(plugin.getPluginFromPath(resolvePath(plugin.getPathToJar())));
    }
    return resolved;
  }

  private Set<FileInfo> postProcessFiles(Set<FileInfo> resolvedFiles) {
    Set<FileInfo> processedFiles = resolvedFiles;
    for (FileParsePostProcessor processor : processors) {
      processedFiles = processor.process(resolvedFiles);
    }
    return processedFiles;
  }
}