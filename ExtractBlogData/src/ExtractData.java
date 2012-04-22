import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtractData implements Runnable {

	private static ConcurrentLinkedQueue<String> allUrls = new ConcurrentLinkedQueue<String>();

	private static String dataPath = "./workingFeeds";
	private static String dataOutputFolder = "./blogDataSet/";
	private static int threadCount = 200;

	int id;
	private AtomicInteger controlEvent;
	private AtomicInteger urlCount;

	public ExtractData(int id, AtomicInteger controlEvent, AtomicInteger urlCount) {
		this.id = id;
		this.controlEvent = controlEvent;
		this.urlCount = urlCount;
	}

	@Override
	public void run() {
		while (true) {
			String url = allUrls.poll();
			if (url == null) {
				int executorsRemainingCount = this.controlEvent
						.decrementAndGet();
				return;
			}
			try {
				int fileNumber = urlCount.decrementAndGet() + 1;
				System.out.println(this.id + " : " + fileNumber + " : " + url);
				writeData(url, fileNumber);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			}
		}
	}

	private static void readData() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(dataPath));
			String line = null;
			while ((line = in.readLine()) != null) {
				allUrls.add(line.trim());
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void writeData(String url, int fileNumber)
			throws IOException {
		URL targetURL = new URL(url);
		URLConnection c = targetURL.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				c.getInputStream()));
		try {
			String fileName = dataOutputFolder
					+ new Integer(fileNumber).toString() + ".xml";
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			String line = null;
			while ((line = in.readLine()) != null) {
				out.write(line + "\n");
				out.flush();
			}
			out.close();
		} finally {
			in.close();
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new File(dataOutputFolder).mkdir();
		readData();
		ExecutorService threadExecutor = Executors
				.newFixedThreadPool(threadCount);
		AtomicInteger controlEvent = new AtomicInteger(threadCount);
		AtomicInteger urlCount = new AtomicInteger(allUrls.size());
		for (int i = 0; i < threadCount; i++)
			threadExecutor.execute(new ExtractData(i + 1, controlEvent, urlCount));
		threadExecutor.shutdown();
	}

}
