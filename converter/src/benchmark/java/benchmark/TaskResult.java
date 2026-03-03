package benchmark;

import utils.StatsTracker;

enum ErrorKind {
    LEFT_RECURSION,
    MISSING_RULE,
    MULTIPLE_FILES,
    MISSING_FILE,
    COMPILE_ERROR,
    UNKNOWN
}

sealed interface TaskResult permits TaskResult.Success, TaskResult.Failure {
    record Success(StatsTracker tracker) implements TaskResult {}
    record Failure(ErrorKind kind, String message) implements TaskResult {}
}
