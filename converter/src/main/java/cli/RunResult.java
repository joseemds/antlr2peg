package cli;

import java.nio.file.Path;
import utils.StatsTracker;

public record RunResult(Path output, StatsTracker statsTracker) {}
;
