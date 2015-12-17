package WeatherDownloader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader_ru5 {
	
	static private HttpResponse httpResponse;
	static private DefaultHttpClient httpClient = new DefaultHttpClient();

	static private String baseSearchURL = "http://rp5.ru/vsearch.php?lang=cn&txt=";
//	static private String[] citylist = {"上海"};
	static private String baseCityPageURL = "http://rp5.ru";
	static private String startDate = "01.01.2014";
	static private String endDate = "11.12.2015";


	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {

		// read citylist
		List<String> citylist = new ArrayList<String>();
		File file = new File("C:\\Users\\xcgon_000\\Downloads\\data\\weather\\citylist2.txt");
		Scanner scanner = new Scanner(file);
		while (scanner.hasNext()) {
			citylist.add(scanner.next());
		}
		scanner.close();

		for(String cityname : citylist) {
			HttpGet httpGet;
			// search city weather
	        httpGet = new HttpGet(baseSearchURL+cityname);
			httpResponse = httpClient.execute(httpGet);
			String searchHtml = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
			httpGet.releaseConnection();
			Document document = Jsoup.parse(searchHtml);
			Elements searchResults = document.select(".searchResults .srow0 a");
			if(searchResults.isEmpty()) continue;
			Element targetElement = searchResults.get(0);
//			System.out.println(targetElement.text()+", "+targetElement.attr("href"));

			// establish download url
			httpGet = new HttpGet(baseCityPageURL+targetElement.attr("href"));
			httpResponse = httpClient.execute(httpGet);
			String cityPageHtml = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
			httpGet.releaseConnection();
			document = Jsoup.parse(cityPageHtml);
			String weatherHistoryURL = document.select("#archive_link").get(0).attr("href");

			// download weather
//			System.out.println(weatherHistoryURL);
			httpGet = new HttpGet(weatherHistoryURL);
			httpResponse = httpClient.execute(httpGet);
			String historyHtml = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
			httpGet.releaseConnection();
			document = Jsoup.parse(historyHtml);
			String wmo_id = document.select("#toFileMenu .archButton").get(0).attr("onclick");
			Pattern p1 = Pattern.compile("[,()]+");
			String[] result = p1.split(wmo_id);
			wmo_id = result[2];

			HttpPost httpPost = new HttpPost("http://rp5.ru/inc/f_archive.php");
			List <NameValuePair> params = new ArrayList <NameValuePair>();
//			a_date1 01.01.2014
//			a_date2 11.12.2015
//			f_ed3 12
//			f_ed4 12
//			f_ed5 11
//			f_pe 1
//			f_pe1 2
//			lng_id 8
//			wmo_id 58362
			params.add(new BasicNameValuePair("a_date1", startDate));
			params.add(new BasicNameValuePair("a_date2", endDate));
			params.add(new BasicNameValuePair("f_ed3", "12"));
			params.add(new BasicNameValuePair("f_ed4", "12"));
			params.add(new BasicNameValuePair("f_ed5", "11"));
			params.add(new BasicNameValuePair("f_pe", "1"));
			params.add(new BasicNameValuePair("f_pe1", "2"));
			params.add(new BasicNameValuePair("lng_id", "8"));
			params.add(new BasicNameValuePair("wmo_id", wmo_id));
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = httpClient.execute(httpPost);
			String downloadHtml = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
			httpGet.releaseConnection();
			Pattern p2 = Pattern.compile("[']+");
			String[] jsStrings = p2.split(downloadHtml);
			Pattern p3 = Pattern.compile("[<=>]+");
			String downURL = p3.split(jsStrings[1])[2];
//			System.out.println(downURL);

			// write file
			httpGet = new HttpGet(downURL);
			httpResponse = httpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			BufferedInputStream bis = new BufferedInputStream(entity.getContent());
			GZIPInputStream gis = new GZIPInputStream(bis);
			String filePath = "C:\\Users\\xcgon_000\\Downloads\\data\\weather\\rp5_download_data\\"+cityname+".txt";
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
			int inByte;
			System.out.println("Writing to " + filePath);
			System.out.println("......");
			while((inByte = gis.read()) != -1) bos.write(inByte);
			gis.close();
			bos.close();
			System.out.println("Finished.");

			return;

		}
	}

}
