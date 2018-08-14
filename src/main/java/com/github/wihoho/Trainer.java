package com.github.wihoho;

import com.github.wihoho.constant.FeatureType;
import com.github.wihoho.jama.Matrix;
import com.github.wihoho.training.*;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 数据集的元素Matrix是一维的列向量
 * @author Hunteron-cp
 *
 */
@Builder
public class Trainer {
    Metric metric;
    FeatureType featureType;
    FeatureExtraction featureExtraction;
    int numberOfComponents;
    int k; // k specifies the number of neighbour to consider

    ArrayList<Matrix> trainingSet;
    ArrayList<String> trainingLabels;

    ArrayList<ProjectedTrainingMatrix> model;

    public void add(Matrix matrix, String label) {
        if (Objects.isNull(trainingSet)) {
            trainingSet = new ArrayList<>();
            trainingLabels = new ArrayList<>();
        }

        trainingSet.add(matrix);
        trainingLabels.add(label);
    }

    public void addFaceAfterTraining(Matrix matrix, String label) {
        featureExtraction.addFace(matrix, label);
    }

    public void train() throws Exception {
        checkNotNull(metric);
        checkNotNull(featureType);
        checkNotNull(numberOfComponents);
        checkNotNull(trainingSet);
        checkNotNull(trainingLabels);

        switch (featureType) {
            case PCA:
                featureExtraction = new PCA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LDA:
                featureExtraction = new LDA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LPP:
                featureExtraction = new LPP(trainingSet, trainingLabels, numberOfComponents);
                break;
        }

        model = featureExtraction.getProjectedTrainingSet();
    }

    public String recognize(Matrix matrix) {
        Matrix testCase = featureExtraction.getW().transpose().times(matrix.minus(featureExtraction.getMeanMatrix()));
        String result = KNN.assignLabel(model.toArray(new ProjectedTrainingMatrix[0]), testCase, k, metric);
        return result;
    }
    
    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
      if (reference == null) {
        throw new NullPointerException();
      }
      return reference;
    }
    
    //Convert a m by n matrix into a m*n by 1 matrix
    //将m*n矩阵转换为一维列向量
    static Matrix vectorize(Matrix input) {
        int m = input.getRowDimension();
        int n = input.getColumnDimension();

        Matrix result = new Matrix(m * n, 1);
        for (int p = 0; p < n; p++) {
            for (int q = 0; q < m; q++) {
                result.set(p * m + q, 0, input.get(q, p));
            }
        }
        return result;
    }
}
