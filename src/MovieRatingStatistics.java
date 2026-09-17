import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MovieRatingStatistics {

    // =========================
    // MAPPER
    // =========================
    public static class StatisticsMapper
            extends Mapper<Object, Text, Text, Text> {

        private final Text movieId = new Text();
        private final Text result = new Text();

        @Override
        public void map(
                Object key,
                Text value,
                Context context)
                throws IOException, InterruptedException {

            String line = value.toString().trim();

            if (line.isEmpty()) {
                return;
            }

            /*
             * Job 1 output:
             *
             * movieId    count    sum
             *
             * Example:
             * 1          49695    194215.0
             */

            String[] fields = line.split("\\s+");

            if (fields.length >= 3) {

                try {

                    String id = fields[0];

                    long count =
                            Long.parseLong(fields[1]);

                    double sum =
                            Double.parseDouble(fields[2]);

                    if (count > 0) {

                        double average =
                                sum / count;

                        // Round to 2 decimal places
                        average =
                                Math.round(
                                        average * 100.0
                                ) / 100.0;

                        /*
                         * Recommendation criteria:
                         * at least 100 ratings
                         * average rating >= 4.0
                         */

                        if (count >= 100 &&
                                average >= 4.0) {

                            movieId.set(id);

                            /*
                             * Output:
                             * count    average
                             */
                            result.set(
                                    count + "\t" +
                                    average
                            );

                            context.write(
                                    movieId,
                                    result
                            );
                        }
                    }

                } catch (NumberFormatException e) {
                    // Ignore invalid records
                }
            }
        }
    }

    // =========================
    // REDUCER
    // =========================
    public static class StatisticsReducer
            extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(
                Text key,
                Iterable<Text> values,
                Context context)
                throws IOException, InterruptedException {

            for (Text value : values) {

                context.write(
                        key,
                        value
                );
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
                    "Usage: MovieRatingStatistics <input> <output>"
            );

            System.exit(2);
        }

        Configuration conf =
                new Configuration();

        Job job =
                Job.getInstance(
                        conf,
                        "Movie Rating Statistics"
                );

        job.setJarByClass(
                MovieRatingStatistics.class
        );

        // Mapper and Reducer
        job.setMapperClass(
                StatisticsMapper.class
        );

        job.setReducerClass(
                StatisticsReducer.class
        );

        // Mapper output
        job.setMapOutputKeyClass(
                Text.class
        );

        job.setMapOutputValueClass(
                Text.class
        );

        // Final output
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