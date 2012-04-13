import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Version {

	private static String dataPath = "Z:\\blogDataSet\\";

	private static void readDataFromFiles(String path) {
		File folder = new File(path);
		File[] files = folder.listFiles();
		int length = files.length;
		int count = 0;
		for (File f : files) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc;
				doc = dBuilder.parse(f);
				doc.getDocumentElement().normalize();
				NodeList nl = doc.getDocumentElement().getElementsByTagName(
						"description");
				String text = "";
				for (int i = 1; i < nl.getLength(); i++) {
					text += nl.item(i).getTextContent();
				}
				Pattern pattern = Pattern.compile("\\p{Alpha}+");
				Matcher matcher = pattern.matcher(text);
				String newText = "";
				while (matcher.find()) {
					String currentText = matcher.group();
					if (currentText.length() > 2
							&& !(currentText.toUpperCase().equals(currentText)))
						newText += currentText + " ";
				}
				int id = new Integer(f.getName().split(".xml")[0]).intValue();
				addData(id, newText);
				System.out.println(count + "/" + length);
				// System.out.println(newText);
			} catch (SAXException | IOException | ParserConfigurationException e) {
				// e.printStackTrace();
			}
			count++;
		}
	}

	public static void main(String[] args) {
		readDataFromFiles(dataPath);
	}

	private static void addData(int id, String text) {
		Connection con = null;

		String url = "jdbc:mysql://localhost:3306/blogdata";
		String user = "root";
		String password = "root";

		try {
			con = DriverManager.getConnection(url, user, password);
			String query = "INSERT INTO blogdata.data (id, text) VALUES (?, ?)";
			PreparedStatement st = con.prepareStatement(query);
			st.setInt(1, id);
			st.setString(2, text);
			st.execute();
			con.close();
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(Version.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);

		}
	}
}