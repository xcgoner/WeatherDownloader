/**
 * Created by xcgon_000 on 2016/6/6.
 */

package WeatherDownloader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler_tianqihoubao_weather {

    static private HttpResponse httpResponse;
    static private DefaultHttpClient httpClient = new DefaultHttpClient();

    static private String baseSearchURL = "http://www.tianqihoubao.com";
    static private int startYear = 2014;
    static private int startMonth = 1;
    static private int endYear = 2015;
    static private int endMonth = 12;

    static private String baseFilePath = "C:\\Users\\xcgon_000\\Downloads\\data\\weather";

    public static String removeDuplicateWhitespace(CharSequence inputStr) {
        String patternStr = "\\s+";
        String replaceStr = "";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.replaceAll(replaceStr).toString();
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {

        // read citylist
        List<String> citylist = new ArrayList<String>();
        List<String> citylink = new ArrayList<String>();

        HttpGet httpGet;

        httpGet = new HttpGet(baseSearchURL+"/lishi/");
        httpResponse = httpClient.execute(httpGet);
        String mainHtml = EntityUtils.toString(httpResponse.getEntity(), "gb2312");
        httpGet.releaseConnection();
        Document mainDocument = Jsoup.parse(mainHtml);
//        Elements mainResults = mainDocument.select(".citychk dl:gt(0) a");
        Elements mainResults = mainDocument.select(".citychk dd a");
        for(Element element : mainResults) {
//            System.out.println(element.text());
            citylist.add(element.text());
            citylink.add(element.attr("href"));
            System.out.println(element.text());
            System.out.println(element.attr("href"));

            // write file
            String filePath = baseFilePath+"\\tianqihoubao_weather\\"+element.text()+".txt";

            File f = new File(filePath);
            if (!f.exists()) {
                FileWriter fw = new FileWriter(filePath,false);

                // crawl each city
                String baseLishiURL = element.attr("href");
                baseLishiURL = baseSearchURL+ baseLishiURL.substring(0, baseLishiURL.length()-5);
//            System.out.println(baseLishiURL);
                int year = startYear, month = startMonth;
                while(year<=endYear && month<=endMonth) {
                    String curURL = baseLishiURL + "/month/" +String.format("%04d%02d.html", year, month);
//                System.out.println(curURL);
                    httpGet = new HttpGet(removeDuplicateWhitespace(curURL));
                    httpResponse = httpClient.execute(httpGet);
                    String wHtml = EntityUtils.toString(httpResponse.getEntity(), "gb2312");
                    httpGet.releaseConnection();
                    Document wDocument = Jsoup.parse(wHtml);
                    Elements wResults = wDocument.select(".wdetail tr:gt(0)");
                    for(Element wtr : wResults) {
                        Elements wtds = wtr.select("td");
                        for(Element wtd : wtds) {
                            fw.write(wtd.text()+",");
                            System.out.print(wtd.text()+",");
                        }
                        fw.write("\n");
                        System.out.println();
                    }

                    month++;
                    if(month == 13) {
                        month = 1;
                        year++;
                    }
                    Thread.sleep((long)(Math.random() * 2000));
                }
                Thread.sleep((long)(Math.random() * 5000));
                fw.close();
            }


        }

    }

}
