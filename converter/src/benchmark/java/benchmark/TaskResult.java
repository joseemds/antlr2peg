package benchmark;

import cli.RunResult;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

enum ErrorKind {
  LEFT_RECURSION,
  MISSING_RULE,
  MULTIPLE_FILES,
  MISSING_FILE,
  WRONG_START_RULE,
  COMPILE_ERROR,
  HAS_SEMANTIC_ACTION,
  UNKNOWN
}

@JsonSerialize(using = TaskResultSerializer.class)
sealed interface TaskResult permits TaskResult.Success, TaskResult.Failure {
  record Success(RunResult result) implements TaskResult {}

  record Failure(ErrorKind kind, String message) implements TaskResult {}
}
