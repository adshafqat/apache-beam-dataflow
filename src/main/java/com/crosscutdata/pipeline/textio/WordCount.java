
package com.crosscutdata.pipeline.textio;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class WordCount {

  static class ExtractWordsFn extends DoFn<String, String> {
    private final Counter emptyLines = Metrics.counter(ExtractWordsFn.class, "emptyLines");
    private final Distribution lineLenDist =
        Metrics.distribution(ExtractWordsFn.class, "lineLenDistro");

    @ProcessElement
    public void processElement(@Element String element, OutputReceiver<String> receiver) {
      lineLenDist.update(element.length());
      if (element.trim().isEmpty()) {
        emptyLines.inc();
      }

      // Split the line into words.
      String[] words = element.split("[^\\p{L}]+", -1);

      // Output each word encountered into the output PCollection.
      for (String word : words) {
        if (!word.isEmpty()) {
          receiver.output(word);
        }
      }
    }
  }
  // [END extract_words_fn]

  /** A SimpleFunction that converts a Word and Count into a printable string. */
  public static class FormatAsTextFn extends SimpleFunction<KV<String, Long>, String> {
    @Override
    public String apply(KV<String, Long> input) {
      return input.getKey() + ": " + input.getValue();
    }
  }

  /**
   * A PTransform that converts a PCollection containing lines of text into a PCollection of
   * formatted word counts.
   *
   * <p>Concept #3: This is a custom composite transform that bundles two transforms (ParDo and
   * Count) as a reusable PTransform subclass. Using composite transforms allows for easy reuse,
   * modular testing, and an improved monitoring experience.
   */
  // [START count_words]
  public static class CountWords
      extends PTransform<PCollection<String>, PCollection<KV<String, Long>>> {
    @Override
    public PCollection<KV<String, Long>> expand(PCollection<String> lines) {

      // Convert lines of text into individual words.
      PCollection<String> words = lines.apply(ParDo.of(new ExtractWordsFn()));

      // Count the number of times each word occurs.
      PCollection<KV<String, Long>> wordCounts = words.apply(Count.perElement());

      return wordCounts;
    }
  }
  // [END count_words]

  /**
   * Options supported by {@link WordCount}.
   *
   * <p>Concept #4: Defining your own configuration options. Here, you can add your own arguments to
   * be processed by the command-line parser, and specify default values for them. You can then
   * access the options values in your pipeline code.
   *
   * <p>Inherits standard configuration options.
   */
  // [START wordcount_options]
  public interface WordCountOptions extends PipelineOptions {

    /**
     * By default, this example reads from a public dataset containing the text of King Lear. Set
     * this option to choose a different input file or glob.
     */
    @Description("Path of the file to read from")
    @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
    String getInputFile();

    void setInputFile(String value);

    /** Set this required option to specify where to write the output. */
    @Description("Path of the file to write to")
    @Required
    String getOutput();

    void setOutput(String value);
  }
  // [END wordcount_options]

  static void runWordCount(WordCountOptions options) {
    Pipeline p = Pipeline.create(options);

    // Concepts #2 and #3: Our pipeline applies the composite CountWords transform, and passes the
    // static FormatAsTextFn() to the ParDo transform.
    p.apply("ReadLines", TextIO.read().from(options.getInputFile()))
        .apply(new CountWords())
        .apply(MapElements.via(new FormatAsTextFn()))
        .apply("WriteCounts", TextIO.write().to(options.getOutput()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    WordCountOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(WordCountOptions.class);

    runWordCount(options);
  }
}

