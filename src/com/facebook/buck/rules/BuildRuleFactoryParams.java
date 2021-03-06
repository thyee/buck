/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rules;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildFileTree;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.parser.BuildTargetParser;
import com.facebook.buck.parser.ParseContext;

public final class BuildRuleFactoryParams {

  private final ProjectFilesystem filesystem;
  public final BuildTargetParser buildTargetParser;
  public final BuildTarget target;
  private final ParseContext buildFileParseContext;
  private final RuleKeyBuilderFactory ruleKeyBuilderFactory;
  private final BuildFileTree buildFileTree;
  private final boolean enforceBuckPackageBoundary;

  public BuildRuleFactoryParams(
      ProjectFilesystem filesystem,
      BuildTargetParser buildTargetParser,
      BuildTarget target,
      RuleKeyBuilderFactory ruleKeyBuilderFactory,
      BuildFileTree buildFileTree,
      boolean enforceBuckPackageBoundary) {
    this.filesystem = filesystem;
    this.buildTargetParser = buildTargetParser;
    this.target = target;
    this.buildFileParseContext = ParseContext.forBaseName(target.getBaseName());
    this.ruleKeyBuilderFactory = ruleKeyBuilderFactory;
    this.buildFileTree = buildFileTree;
    this.enforceBuckPackageBoundary = enforceBuckPackageBoundary;
  }

  public BuildTarget resolveBuildTarget(String target) {
    return buildTargetParser.parse(target, buildFileParseContext);
  }

  public ProjectFilesystem getProjectFilesystem() {
    return filesystem;
  }

  public RuleKeyBuilderFactory getRuleKeyBuilderFactory() {
    return ruleKeyBuilderFactory;
  }

  public BuildFileTree getBuildFileTree() {
    return buildFileTree;
  }

  public boolean enforceBuckPackageBoundary() {
    return enforceBuckPackageBoundary;
  }
}
