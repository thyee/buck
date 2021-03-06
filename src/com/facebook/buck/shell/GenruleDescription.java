/*
 * Copyright 2014-present Facebook, Inc.
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

package com.facebook.buck.shell;


import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.parser.BuildTargetParser;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.ImplicitDepsInferringDescription;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.macros.ExecutableMacroExpander;
import com.facebook.buck.rules.macros.LocationMacroExpander;
import com.facebook.buck.rules.macros.MacroException;
import com.facebook.buck.rules.macros.MacroExpander;
import com.facebook.buck.rules.macros.MacroHandler;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.infer.annotation.SuppressFieldNotInitialized;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

public class GenruleDescription
    implements
    Description<GenruleDescription.Arg>,
    ImplicitDepsInferringDescription<GenruleDescription.Arg> {

  public static final BuildRuleType TYPE = new BuildRuleType("genrule");

  private final MacroHandler macroHandler;

  public GenruleDescription() {
    BuildTargetParser parser = new BuildTargetParser();
    this.macroHandler = new MacroHandler(
        ImmutableMap.<String, MacroExpander>of(
            "exe", new ExecutableMacroExpander(parser),
            "location", new LocationMacroExpander(parser)));
  }

  @Override
  public BuildRuleType getBuildRuleType() {
    return TYPE;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public <A extends Arg> Genrule createBuildRule(
      BuildRuleParams params,
      BuildRuleResolver resolver,
      A args) {
    SourcePathResolver pathResolver = new SourcePathResolver(resolver);

    ImmutableList<SourcePath> srcs = args.srcs.get();
    ImmutableSortedSet<BuildRule> extraDeps = ImmutableSortedSet.<BuildRule>naturalOrder()
        .addAll(params.getExtraDeps())
        .addAll(pathResolver.filterBuildRuleInputs(srcs))
        .build();
    return new Genrule(
        params.copyWithExtraDeps(extraDeps),
        pathResolver,
        srcs,
        macroHandler.getExpander(
            params.getBuildTarget(),
            resolver,
            params.getProjectFilesystem()),
        args.cmd,
        args.bash,
        args.cmdExe,
        args.out,
        params.getPathAbsolutifier());
  }

  @Override
  public Iterable<String> findDepsForTargetFromConstructorArgs(
      BuildTarget buildTarget,
      GenruleDescription.Arg constructorArg) {
    ImmutableSet.Builder<String> targets = ImmutableSet.builder();
    if (constructorArg.bash.isPresent()) {
      addDepsFromParam(buildTarget, constructorArg.bash.get(), targets);
    }
    if (constructorArg.cmd.isPresent()) {
      addDepsFromParam(buildTarget, constructorArg.cmd.get(), targets);
    }
    if (constructorArg.cmdExe.isPresent()) {
      addDepsFromParam(buildTarget, constructorArg.cmdExe.get(), targets);
    }
    return targets.build();
  }

  private void addDepsFromParam(
      BuildTarget target,
      String paramValue,
      ImmutableSet.Builder<String> targets) {
        try {
          targets.addAll(
              Iterables.transform(
                  macroHandler.extractTargets(
                      target,
                      paramValue),
                  Functions.toStringFunction()));
        } catch (MacroException e) {
          throw new HumanReadableException(e, "%s: %s", target, e.getMessage());
    }
  }

  @SuppressFieldNotInitialized
  public class Arg {
    public String out;
    public Optional<String> bash;
    public Optional<String> cmd;
    public Optional<String> cmdExe;
    public Optional<ImmutableList<SourcePath>> srcs;

    public Optional<ImmutableSortedSet<BuildTarget>> deps;
  }
}
