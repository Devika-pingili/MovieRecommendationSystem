import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MovieRatingAnalysis {

    // =========================
    // MAPPER
    // =========================
    public static class RatingMapper
            extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        private final Text movieId = new Text();
        private final DoubleWritable rating = new DoubleWritable();

        @Override
        public void map(
                LongWritable key,
                Text value,
                Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // Skip CSV header
            if (line.startsWith("userId")) {
                return;
            }

            String[] fields = line.split(",");

            // Expected:
            // userId,movieId,rating,timestamp
            if (fields.length >= 3) {

                try {
                    String id = fields[1];
                    double rate = Double.parseDouble(fields[2]);

                    movieId.set(id);
                    rating.set(rate);

                    // movieId -> rating
                    context.write(movieId, rating);

                } catch (NumberFormatException e) {
                    // Ignore invalid records
                }
            }
        }
    }

    // =========================
    // REDUCER
    // =========================
    public static class RatingReducer
            extends Reducer<Text, DoubleWritable, Text, Text> {

        private final Text result = new Text();

        @Override
        public void reduce(
                Text key,
                Iterable<DoubleWritable> values,
                Context context)
                throws IOException, InterruptedException {

            double sum = 0.0;
            long count = 0;

            for (DoubleWritable value : values) {

                sum += value.get();
                count++;
            }

            if (count > 0) {

                // Output:
                // movieId    count    sum
                result.set(
                        count + "\t" +
                        Math.round(sum * 100.0) / 100.0
                );

                context.write(key, result);
            }
        }
    }

    // =========================
    // DRIVER
    // =========================
    public static void main(String[] args)
            throws Exception {

        if (args.length != 2) {

            System.err.println(
                    "Usage: MovieRatingAnalysis <input> <output>"
            );

            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(
                conf,
                "Movie Rating Analysis"
        );

        job.setJarByClass(
                MovieRatingAnalysis.class
        );

        // Mapper and Reducer
        job.setMapperClass(
                RatingMapper.class
        );

        job.setReducerClass(
                RatingReducer.class
        );

        // Mapper output types
        job.setMapOutputKeyClass(
                Text.class
        );

        job.setMapOutputValueClass(
                DoubleWritable.class
        );

        // Final output types
        job.setOutputKeyClass(
                Text.class
        );

        job.setOutputValueClass(
                Text.class
        );

        // Input
        FileInputFormat.addInputPath(
                job,
                new Path(args[0])
        );

        // Output
        FileOutputFormat.setOutputPath(
                job,
                new Path(args[1])
        );

        System.exit(
                job.waitForCompletion(true)
                        ? 0
                        : 1
        );
    }
}