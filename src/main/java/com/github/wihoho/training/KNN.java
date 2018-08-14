package com.github.wihoho.training;

import com.github.wihoho.jama.Matrix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 就是在训练集中数据和标签已知的情况下，输入测试数据，将测试数据的特征与训练集中对应的特征进行相互比较，
 * 找到训练集中与之最为相似的前K个数据，则该测试数据对应的类别就是K个数据中出现次数最多的那个分类
 * @author Hunteron-cp
 *
 */
public class KNN {

	public static String assignLabel(ProjectedTrainingMatrix[] trainingSet, Matrix testFace, int K, Metric metric) {
		ProjectedTrainingMatrix[] neighbors = findKNN(trainingSet, testFace, K, metric);
		return classify(neighbors);
	}

	// testFace has been projected to the subspace
	static ProjectedTrainingMatrix[] findKNN(ProjectedTrainingMatrix[] trainingSet, Matrix testFace, int K, Metric metric) {
		int NumOfTrainingSet = trainingSet.length;
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";

		// initialization
		ProjectedTrainingMatrix[] neighbors = new ProjectedTrainingMatrix[K];
		int i;
		for (i = 0; i < K; i++) {
			trainingSet[i].distance = metric.getDistance(trainingSet[i].matrix,
					testFace);
//			System.out.println("index: " + i + " distance: "
//					+ trainingSet[i].distance);
			neighbors[i] = trainingSet[i];
		}

		// go through the remaining records in the trainingSet to find K nearest
		// neighbors
		for (i = K; i < NumOfTrainingSet; i++) {
			trainingSet[i].distance = metric.getDistance(trainingSet[i].matrix,
					testFace);
//			System.out.println("index: " + i + " distance: "
//					+ trainingSet[i].distance);

			int maxIndex = 0;
			for (int j = 0; j < K; j++) {
				if (neighbors[j].distance > neighbors[maxIndex].distance)
					maxIndex = j;
			}

			if (neighbors[maxIndex].distance > trainingSet[i].distance)
				neighbors[maxIndex] = trainingSet[i];
		}
		return neighbors;
	}

	// get the class label by using neighbors
	static String classify(ProjectedTrainingMatrix[] neighbors) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		int num = neighbors.length;

		for (int index = 0; index < num; index++) {
			ProjectedTrainingMatrix temp = neighbors[index];
			String key = temp.label;
			if (!map.containsKey(key))
				map.put(key, 1 / temp.distance);
			else {
				double value = map.get(key);
				value += 1 / temp.distance;
				map.put(key, value);
			}
		}

		// Find the most likely label
		double maxSimilarity = 0;
		String returnLabel = "";
		Set<String> labelSet = map.keySet();
		Iterator<String> it = labelSet.iterator();
		while (it.hasNext()) {
			String label = it.next();
			double value = map.get(label);
			if (value > maxSimilarity) {
				maxSimilarity = value;
				returnLabel = label;
			}
		}

		return returnLabel;
	}
}
