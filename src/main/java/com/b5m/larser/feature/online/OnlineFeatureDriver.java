package com.b5m.larser.feature.online;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b5m.lr.ListWritable;

public class OnlineFeatureDriver {
	private static final Logger LOG = LoggerFactory
			.getLogger(OnlineFeatureDriver.class);

	public static long run(Path input, Path output, Configuration baseConf)
			throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration(baseConf);
		Job job = Job.getInstance(conf);

		job.setJarByClass(OnlineFeatureDriver.class);
		job.setJobName("GROUP each user's feature BY uuid");

		FileInputFormat.setInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, output);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(VectorWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ListWritable.class);

		job.setMapperClass(OnlineFeatureMapper.class);
		job.setReducerClass(OnlineFeatureReducer.class);

		HadoopUtil.delete(conf, output);
		boolean succeeded = job.waitForCompletion(true);
		if (!succeeded) {
			throw new IllegalStateException(
					"Job:Group each user's feature,  Failed!");
		}
		Counter counter = job.getCounters().findCounter(
				"org.apache.hadoop.mapred.Task$Counter",
				"REDUCE_OUTPUT_RECORDS");
		long reduceOutputRecords = counter.getValue();

		LOG.info(
				"Job: GROUP each user's feature BY uuid, output recordes = {}",
				reduceOutputRecords);
		
		return reduceOutputRecords;
	}
}
