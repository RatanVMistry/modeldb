package ai.verta.modeldb.versioning.blob.diff;

import ai.verta.modeldb.versioning.DiffStatusEnum.DiffStatus;
import ai.verta.modeldb.versioning.autogenerated._public.modeldb.versioning.model.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: autogenerate?
// TODO: equal comparison should compare just immediate A and B, instead of recursing. This happens
// because some diff types are not isolated
public class DiffComputer {
  public static <B, F, R> Optional<R> computeDiff(
      Optional<B> a,
      Optional<B> b,
      Function<B, Optional<F>> getter,
      Function3<Optional<F>, Optional<F>, Optional<R>> computer) {
    return computer.apply(a.flatMap(getter::apply), b.flatMap(getter::apply));
  }

  // This applies an algorithm similar to what I discussed with Ravi for merges: gather sets based
  // on a key
  // 1. if there is a key collision and both sets have size 1, then compute the diff of those
  // elements
  // 2. if there is a key collision and both sets have more than 1 element, consider all A as
  // removal and B as addition, ignoring any modifications (by passing null values)
  // 3. if there is no key collision, then just process the right side
  public static <B, F, R extends ProtoType> Optional<List<R>> computeListDiff(
      Optional<B> a,
      Optional<B> b,
      Function<B, Optional<List<F>>> getter,
      Function<F, Optional<String>> hasher,
      Function3<Optional<F>, Optional<F>, Optional<R>> computer) {
    HashMap<String, HashSet<F>> mapA = new HashMap<>();
    HashMap<String, HashSet<F>> mapB = new HashMap<>();
    a.flatMap(getter::apply)
        .ifPresent(
            x ->
                x.forEach(
                    el ->
                        hasher
                            .apply(el)
                            .ifPresent(h -> mapA.getOrDefault(h, new HashSet<>()).add(el))));
    b.flatMap(getter::apply)
        .ifPresent(
            x ->
                x.forEach(
                    el ->
                        hasher
                            .apply(el)
                            .ifPresent(h -> mapB.getOrDefault(h, new HashSet<>()).add(el))));

    HashSet<String> keys = new HashSet<>();
    keys.addAll(mapA.keySet());
    keys.addAll(mapB.keySet());

    List<R> ret =
        keys.stream()
            .flatMap(
                key -> {
                  HashSet<F> elA = mapA.get(key);
                  HashSet<F> elB = mapB.get(key);
                  // Key collision and one element, process it
                  if (elA != null && elB != null && elA.size() == 1 && elB.size() == 1) {
                    return Stream.of(
                        computer.apply(
                            Optional.of(elA.iterator().next()),
                            Optional.of(elB.iterator().next())));
                  }

                  // Key collision and more elements, consider removal + addition
                  if (elA != null && elB != null) {
                    return Stream.concat(
                        elA.stream().map(el -> computer.apply(Optional.of(el), Optional.empty())),
                        elB.stream().map(el -> computer.apply(Optional.empty(), Optional.of(el))));
                  } else if (elA != null) {
                    return elA.stream()
                        .map(el -> computer.apply(Optional.of(el), Optional.empty()));
                  } else {
                    return elB.stream()
                        .map(el -> computer.apply(Optional.empty(), Optional.of(el)));
                  }
                })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (ret.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(ret);
  }

  public static <T, T2> Optional<T2> initDiff(Optional<T> a, Optional<T> b, T2 obj) {
    if (!a.isPresent() && !b.isPresent()) return Optional.empty();
    if (a.isPresent() && b.isPresent() && a.equals(b)) return Optional.empty();
    return Optional.of(obj);
  }

  public static <T, T2> Optional<T2> eitherGet(
      Optional<T> a, Optional<T> b, Function<T, Optional<T2>> getter) {
    if (a.isPresent()) return a.flatMap(getter::apply);
    if (b.isPresent()) return b.flatMap(getter::apply);
    return Optional.empty();
  }

  public static <T> DiffStatusEnumDiffStatus getStatus(Optional<T> a, Optional<T> b) {
    if (!a.isPresent() && !b.isPresent()) {
      return null;
    }
    if (!a.isPresent()) {
      return new DiffStatusEnumDiffStatus(DiffStatus.ADDED);
    }
    if (!b.isPresent()) {
      return new DiffStatusEnumDiffStatus(DiffStatus.DELETED);
    }
    return new DiffStatusEnumDiffStatus(DiffStatus.MODIFIED);
  }

  public static Optional<BlobDiff> computeBlobDiff(Optional<Blob> a, Optional<Blob> b) {
    return Utils.removeEmpty(
        new BlobDiff()
            .setCode(computeDiff(a, b, x -> x.Code, DiffComputer::computeCodeDiff))
            .setConfig(computeDiff(a, b, x -> x.Config, DiffComputer::computeConfigDiff))
            .setDataset(computeDiff(a, b, x -> x.Dataset, DiffComputer::computeDatasetDiff))
            .setEnvironment(
                computeDiff(a, b, x -> x.Environment, DiffComputer::computeEnvironmentDiff)));
  }

  public static Optional<CodeDiff> computeCodeDiff(Optional<CodeBlob> a, Optional<CodeBlob> b) {
    return Utils.removeEmpty(
        new CodeDiff()
            .setGit(computeDiff(a, b, x -> x.Git, DiffComputer::computeGitCodeDiff))
            .setNotebook(
                computeDiff(a, b, x -> x.Notebook, DiffComputer::computeNotebookCodeDiff)));
  }

  public static Optional<GitCodeDiff> computeGitCodeDiff(
      Optional<GitCodeBlob> a, Optional<GitCodeBlob> b) {
    return Utils.removeEmpty(
        initDiff(a, b, new GitCodeDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<NotebookCodeDiff> computeNotebookCodeDiff(
      Optional<NotebookCodeBlob> a, Optional<NotebookCodeBlob> b) {
    return Utils.removeEmpty(
        new NotebookCodeDiff()
            .setGitRepo(computeDiff(a, b, x -> x.GitRepo, DiffComputer::computeGitCodeDiff))
            .setPath(
                computeDiff(a, b, x -> x.Path, DiffComputer::computePathDatasetComponentDiff)));
  }

  public static Optional<ConfigDiff> computeConfigDiff(
      Optional<ConfigBlob> a, Optional<ConfigBlob> b) {
    return Utils.removeEmpty(
        new ConfigDiff()
            .setHyperparameters(
                computeListDiff(
                    a,
                    b,
                    x -> x.Hyperparameters,
                    x -> x.Name,
                    DiffComputer::computeHyperparameterConfigDiff))
            .setHyperparameterSet(
                computeListDiff(
                    a,
                    b,
                    x -> x.HyperparameterSet,
                    x -> x.Name,
                    DiffComputer::computeHyperparameterSetConfigDiff)));
  }

  public static Optional<HyperparameterConfigDiff> computeHyperparameterConfigDiff(
      Optional<HyperparameterConfigBlob> a, Optional<HyperparameterConfigBlob> b) {
    return Utils.removeEmpty(
        initDiff(
            a,
            b,
            new HyperparameterConfigDiff()
                .setName(eitherGet(a, b, x -> x.Name))
                .setA(a.flatMap(x -> x.Value))
                .setB(b.flatMap(x -> x.Value))
                .setStatus(getStatus(a, b))));
  }

  public static Optional<HyperparameterSetConfigDiff> computeHyperparameterSetConfigDiff(
      Optional<HyperparameterSetConfigBlob> a, Optional<HyperparameterSetConfigBlob> b) {
    return Utils.removeEmpty(
        initDiff(
            a,
            b,
            new HyperparameterSetConfigDiff()
                .setName(eitherGet(a, b, x -> x.Name))
                .setContinuousA(a.flatMap(x -> x.Continuous))
                .setContinuousB(b.flatMap(x -> x.Continuous))
                .setDiscreteA(a.flatMap(x -> x.Discrete))
                .setDiscreteB(b.flatMap(x -> x.Discrete))
                .setStatus(getStatus(a, b))));
  }

  public static Optional<DatasetDiff> computeDatasetDiff(
      Optional<DatasetBlob> a, Optional<DatasetBlob> b) {
    return Utils.removeEmpty(
        new DatasetDiff()
            .setPath(computeDiff(a, b, x -> x.Path, DiffComputer::computePathDatasetDiff))
            .setS3(computeDiff(a, b, x -> x.S3, DiffComputer::computeS3DatasetDiff)));
  }

  public static Optional<PathDatasetDiff> computePathDatasetDiff(
      Optional<PathDatasetBlob> a, Optional<PathDatasetBlob> b) {
    return Utils.removeEmpty(
        new PathDatasetDiff()
            .setComponents(
                computeListDiff(
                    a,
                    b,
                    x -> x.Components,
                    x -> x.Path,
                    DiffComputer::computePathDatasetComponentDiff)));
  }

  public static Optional<PathDatasetComponentDiff> computePathDatasetComponentDiff(
      Optional<PathDatasetComponentBlob> a, Optional<PathDatasetComponentBlob> b) {
    return Utils.removeEmpty(
        initDiff(a, b, new PathDatasetComponentDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<S3DatasetDiff> computeS3DatasetDiff(
      Optional<S3DatasetBlob> a, Optional<S3DatasetBlob> b) {
    return Utils.removeEmpty(
        new S3DatasetDiff()
            .setComponents(
                computeListDiff(
                    a,
                    b,
                    x -> x.Components,
                    x -> x.Path.flatMap(y -> y.Path),
                    DiffComputer::computeS3DatasetComponentDiff)));
  }

  public static Optional<S3DatasetComponentDiff> computeS3DatasetComponentDiff(
      Optional<S3DatasetComponentBlob> a, Optional<S3DatasetComponentBlob> b) {
    return Utils.removeEmpty(
        new S3DatasetComponentDiff()
            .setPath(
                computeDiff(a, b, x -> x.Path, DiffComputer::computePathDatasetComponentDiff)));
  }

  public static Optional<EnvironmentDiff> computeEnvironmentDiff(
      Optional<EnvironmentBlob> a, Optional<EnvironmentBlob> b) {
    return Utils.removeEmpty(
        new EnvironmentDiff()
            .setCommandLine(
                computeDiff(
                    a, b, x -> x.CommandLine, DiffComputer::computeCommandLineEnvironmentDiff))
            .setDocker(computeDiff(a, b, x -> x.Docker, DiffComputer::computeDockerEnvironmentDiff))
            .setPython(computeDiff(a, b, x -> x.Python, DiffComputer::computePythonEnvironmentDiff))
            .setEnvironmentVariables(
                computeListDiff(
                    a,
                    b,
                    x -> x.EnvironmentVariables,
                    x -> x.Name,
                    DiffComputer::computeEnvironmentVariablesDiff)));
  }

  public static Optional<CommandLineEnvironmentDiff> computeCommandLineEnvironmentDiff(
      Optional<List<String>> a, Optional<List<String>> b) {
    return Utils.removeEmpty(
        initDiff(
            a, b, new CommandLineEnvironmentDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<DockerEnvironmentDiff> computeDockerEnvironmentDiff(
      Optional<DockerEnvironmentBlob> a, Optional<DockerEnvironmentBlob> b) {
    return Utils.removeEmpty(
        initDiff(a, b, new DockerEnvironmentDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<PythonEnvironmentDiff> computePythonEnvironmentDiff(
      Optional<PythonEnvironmentBlob> a, Optional<PythonEnvironmentBlob> b) {
    return Utils.removeEmpty(
        new PythonEnvironmentDiff()
            .setVersion(
                computeDiff(a, b, x -> x.Version, DiffComputer::computeVersionEnvironmentDiff))
            .setConstraints(
                computeListDiff(
                    a,
                    b,
                    x -> x.Constraints,
                    x -> x.Library,
                    DiffComputer::computePythonRequirementEnvironmentDiff))
            .setRequirements(
                computeListDiff(
                    a,
                    b,
                    x -> x.Requirements,
                    x -> x.Library,
                    DiffComputer::computePythonRequirementEnvironmentDiff)));
  }

  public static Optional<VersionEnvironmentDiff> computeVersionEnvironmentDiff(
      Optional<VersionEnvironmentBlob> a, Optional<VersionEnvironmentBlob> b) {
    return Utils.removeEmpty(
        initDiff(a, b, new VersionEnvironmentDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<PythonRequirementEnvironmentDiff> computePythonRequirementEnvironmentDiff(
      Optional<PythonRequirementEnvironmentBlob> a, Optional<PythonRequirementEnvironmentBlob> b) {
    return Utils.removeEmpty(
        initDiff(
            a,
            b,
            new PythonRequirementEnvironmentDiff().setA(a).setB(b).setStatus(getStatus(a, b))));
  }

  public static Optional<EnvironmentVariablesDiff> computeEnvironmentVariablesDiff(
      Optional<EnvironmentVariablesBlob> a, Optional<EnvironmentVariablesBlob> b) {
    return Utils.removeEmpty(
        initDiff(
            a,
            b,
            new EnvironmentVariablesDiff()
                .setName(eitherGet(a, b, x -> x.Name))
                .setValueA(a.flatMap(x -> x.Value))
                .setValueB(b.flatMap(x -> x.Value))
                .setStatus(getStatus(a, b))));
  }
}
