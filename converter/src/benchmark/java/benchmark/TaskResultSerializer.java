package benchmark;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import utils.StatsTracker;

public class TaskResultSerializer extends StdSerializer<TaskResult> {

  public TaskResultSerializer() {
    super(TaskResult.class);
  }

  @Override
  public void serialize(TaskResult value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    gen.writeStartObject();

    switch (value) {
      case TaskResult.Success s -> {
        gen.writeStringField("status", "success");
        gen.writeStringField("output", s.result().output().toString());
        provider
            .findValueSerializer(StatsTracker.class)
            .unwrappingSerializer(null)
            .serialize(s.result().statsTracker(), gen, provider);
      }
      case TaskResult.Failure f -> {
        gen.writeStringField("status", "failure");
        gen.writeStringField("error_kind", f.kind().name());
        gen.writeStringField("error_message", f.message());
      }
    }

    gen.writeEndObject();
  }
}
