package edu.handong.csee.histudy.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
public class CSVResolver {

  private final List<CSVRecord> records;

  public static CSVResolver of(MultipartFile file) {
    try (InputStream in = file.getInputStream()) {
      Reader reader =
          new InputStreamReader(
              new BOMInputStream.Builder().setInputStream(in).get(), StandardCharsets.UTF_8);
      CSVFormat format =
          CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().setSkipHeaderRecord(true).build();
      CSVParser parser = CSVParser.parse(reader, format);
      CSVResolver resolver = new CSVResolver(parser.getRecords());

      parser.close();
      reader.close();
      return resolver;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<CourseCSV> resolve() {
    return records.stream().map(CourseCSV::of).toList();
  }
}
