/*
 * Copyright 2010 Google Inc.
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

jstestdriver.LoadTestsCommand = function(
    jsonParse,
    pluginRegistrar,
    getBrowserInfo,
    onLoadComplete) {
  this.jsonParse_ = jsonParse;
  this.pluginRegistrar_ = pluginRegistrar;
  this.boundOnFileLoaded_ = jstestdriver.bind(this, this.onFileLoaded);
  this.boundOnFileLoadedRunnerMode_ =
      jstestdriver.bind(this, this.onFileLoadedRunnerMode);
  this.getBrowserInfo = getBrowserInfo;
  this.onLoadComplete_ = onLoadComplete
}


jstestdriver.LoadTestsCommand.prototype.loadTest = function(args) {
  var files = args[0];
  var runnerMode = args[1] == "true" ? true : false;
  var fileSrcs = this.jsonParse_('{"f":' + files + '}').f;

  this.removeScripts(document, fileSrcs);
  var fileLoader = new jstestdriver.FileLoader(this.pluginRegistrar_,
       this.boundOnFileLoaded_);

  fileLoader.load(fileSrcs);
};


jstestdriver.LoadTestsCommand.prototype.onFileLoaded = function(status) {
  var response = new jstestdriver.Response(
      jstestdriver.RESPONSE_TYPES.FILE_LOAD_RESULT,
      JSON.stringify(status),
      this.getBrowserInfo());
  this.onLoadComplete_(response);
};

jstestdriver.LoadTestsCommand.prototype.onFileLoadedRunnerMode = function(status) {
  this.streamingService_.close(null, this.__boundExecuteCommand);
};

jstestdriver.LoadTestsCommand.prototype.findScriptTagsToRemove_ = function(dom, fileSrcs) {
  var scripts = dom.getElementsByTagName('script');
  var filesSize = fileSrcs.length;
  var scriptsSize = scripts.length;
  var scriptTagsToRemove = [];

  for (var i = 0; i < filesSize; i++) {
    var f = fileSrcs[i].fileSrc;

    for (var j = 0; j < scriptsSize; j++) {
      var s = scripts[j];

      if (s.src.indexOf(f) != -1) {
        scriptTagsToRemove.push(s);
        break;
      }
    }
  }
  return scriptTagsToRemove;
};


jstestdriver.LoadTestsCommand.prototype.removeScriptTags_ = function(dom,
                                                                    scriptTagsToRemove) {
  var head = dom.getElementsByTagName('head')[0];
  var size = scriptTagsToRemove.length;

  for (var i = 0; i < size; i++) {
    var script = scriptTagsToRemove[i];

    head.removeChild(script);
  }
};


jstestdriver.LoadTestsCommand.prototype.removeScripts = function(dom, fileSrcs) {
  this.removeScriptTags_(dom, this.findScriptTagsToRemove_(dom, fileSrcs));
};
