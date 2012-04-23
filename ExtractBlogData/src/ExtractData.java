import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtractData implements Runnable {

	private static ConcurrentLinkedQueue<String> allUrls = new ConcurrentLinkedQueue<String>();

	private static String dataPath = "./good_feeds.txt";
	private static String dataOutputFolder = "./blogDataSet/";
	private static int threadCount = 200;
	private static int start_index = 0;
	private static int count = Integer.MAX_VALUE;

	int id;
	private AtomicInteger controlEvent;
	private AtomicInteger urlCount;

	public ExtractData(int id, AtomicInteger controlEvent,
			AtomicInteger urlCount) {
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
				int fileNumber = start_index + urlCount.decrementAndGet() + 1;
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
			int c = 0;
			while ((line = in.readLine()) != null) {
				if (c >= start_index) {
					String[] values = line.trim().split("\t");
					if (values.length == 2) {
						String targetUrl = values[1];
						if (!values[1].toLowerCase().startsWith("http")) {
							URL url = new URL(values[0]);
							URL newUrl = new URL(url.getProtocol(),
									url.getHost(), values[1]);
							targetUrl = newUrl.toString();
						}
						allUrls.add(targetUrl);
						if (c >= start_index - 1 + count)
							break;
					}
				}
				c++;
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void writeData(String url, int fileNumber)
			throws IOException {
		BufferedReader in = null;
		BufferedWriter out = null;
		String fileName = null;
		try {
			URL targetURL = new URL(url);
			URLConnection c = targetURL.openConnection();
			in = new BufferedReader(new InputStreamReader(c.getInputStream()));

			fileName = dataOutputFolder + new Integer(fileNumber).toString()
					+ ".xml";
			out = new BufferedWriter(new FileWriter(fileName));
			String line = null;
			boolean isAscii = true;
			boolean isHtml = false;
			while ((line = in.readLine()) != null) {
				String org_line = line;
				line = new String(line.getBytes(Charset.forName("US-ASCII")));
				if (!line.matches("[ -~\\s]*")) {
					isAscii = false;
					break;
				}
				String lline = line.toLowerCase();
				if (lline.contains("<html") || lline.contains("<head>")
						|| lline.contains("<body>")) {
					isHtml = true;
					break;
				}
				out.write(org_line + "\n");
				out.flush();
			}
			out.close();
			out = null;
			in.close();
			in = null;
			if (!isAscii || isHtml) {
				System.out.println("Deleteing: " + url);
				new File(fileName).delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (fileName != null) {
				File f = new File(fileName);
				if (f.exists() && f.length() == 0) {
					System.out.println("Deleteing: " + url);
					f.delete();
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			start_index = new Integer(args[0]).intValue();
			count = new Integer(args[1]).intValue();
		}

		new File(dataOutputFolder).mkdir();
		readData();
		ExecutorService threadExecutor = Executors
				.newFixedThreadPool(threadCount);
		AtomicInteger controlEvent = new AtomicInteger(threadCount);
		AtomicInteger urlCount = new AtomicInteger(allUrls.size());
		for (int i = 0; i < threadCount; i++)
			threadExecutor.execute(new ExtractData(i + 1, controlEvent,
					urlCount));
		threadExecutor.shutdown();
	}

}
