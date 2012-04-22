import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class ProcessData {

	private static final String dataPath = "Z:\\FakeBlogDetection\\CompressedDataset\\data.csv";
	private static final String clusterResPath = "Z:\\FakeBlogDetection\\CompressedDataset\\cluster_res.csv";

	private static HashMap<String, ArrayList<Integer>> getClusterResult() {
		HashMap<String, ArrayList<Integer>> entries = new HashMap<String, ArrayList<Integer>>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader((InputStream) new FileInputStream(
							new File(clusterResPath))));
			String line = null;
			line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(";");
				int index = (int) new Double(vals[1]).doubleValue();
				String clusterId = vals[2];
				ArrayList<Integer> cluster = entries.get(clusterId);
				if (cluster == null) {
					cluster = new ArrayList<Integer>();
					entries.put(clusterId, cluster);
				}
				cluster.add(index);
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entries;
	}

	private static String[] getHeaders() {
		String[] headers = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					(InputStream) new FileInputStream(new File(dataPath))));
			String line = null;
			line = reader.readLine();
			line = line.replaceAll("\"", "");
			headers = line.split(";");
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return headers;
	}

	private static double[][] getTermDocumentMatrixArray() {
		ArrayList<double[]> entries = new ArrayList<double[]>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					(InputStream) new FileInputStream(new File(dataPath))));
			String line = null;
			line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(";");
				double[] row = new double[vals.length];
				for (int i = 0; i < row.length; i++) {
					row[i] = new Double(vals[i]).doubleValue();
				}
				entries.add(row);
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double[][] result = new double[entries.size()][];
		for (int i = 0; i < result.length; i++)
			result[i] = entries.get(i);

		// System.out.println("Done with data extraction");
		return result;
	}

	private static void add(double[] sum, double[] vector) {
		for (int i = 0; i < sum.length; i++)
			sum[i] += vector[i];
	}

	private static void multiply(double[] sum, double value) {
		for (int i = 0; i < sum.length; i++)
			sum[i] *= value;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Matrix c = Matrix.constructWithCopy(getTermDocumentMatrixArray());
		getTopNWordsFromClusters(c, 40);
		doSVD(c);
	}

	private static void doSVD(Matrix c) {
		SingularValueDecomposition svd = c.svd();
		Matrix u = svd.getU().getMatrix(0, svd.getU().getRowDimension() - 1, 0,
				svd.rank() - 1);
		Matrix s = svd.getS().getMatrix(0, svd.rank() - 1, 0, svd.rank() - 1);
		Matrix v = svd.getV().getMatrix(0, svd.getV().getRowDimension() - 1, 0,
				svd.rank() - 1);
		System.out.println("Index\tSingular Value");
		int index = 1;
		for (double singularValue : svd.getSingularValues()) {
			System.out.println(index + "\t" + singularValue);
			index++;
		}
	}

	private static void getTopNWordsFromClusters(Matrix c, int topN) {
		HashMap<String, ArrayList<Integer>> clusterResults = getClusterResult();
		double[][] meanVectors = new double[clusterResults.size()][c
				.getColumnDimension()];
		double[][] matrix = c.getArray();
		for (String id : clusterResults.keySet()) {
			ArrayList<Integer> indices = clusterResults.get(id);
			int index = id.charAt(id.length() - 2) - '0';
			for (Integer in : indices) {
				add(meanVectors[index], matrix[in.intValue() - 1]);
			}
			multiply(meanVectors[index], 1.0 / indices.size());
		}
		String[] headers = getHeaders();
		for (int i = 0; i < meanVectors.length; i++) {
			for (int j = 0; j < topN; j++) {
				double maxValue = Double.NEGATIVE_INFINITY;
				int maxIndex = -1;
				for (int k = 0; k < meanVectors[i].length; k++) {
					if (meanVectors[i][k] > maxValue) {
						maxValue = meanVectors[i][k];
						maxIndex = k;
					}
				}
				meanVectors[i][maxIndex] = -1;
				System.out.print(headers[maxIndex] + " ");
			}
			System.out.println();
		}
	}
}
