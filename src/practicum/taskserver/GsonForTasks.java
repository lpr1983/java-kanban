package practicum.taskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonForTasks {

    private GsonForTasks() {
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(localDateTime.format(dtf));
            }
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            if (value == null) {
                return null;
            }
            return LocalDateTime.parse(value, dtf);
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            if (duration == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(duration.toString());
            }
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            String value = jsonReader.nextString();
            if (value == null) {
                return null;
            }
            return Duration.parse(value);
        }
    }
}