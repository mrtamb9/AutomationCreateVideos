package youtube.automation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import parameters.Parameters;
import utils.Utils;

public class CreateVideos {
	
	static String file_many_links = "many_links.txt";
	static String file_one_link = "one_link.txt";
	static String file_driver = "geckodriver/chromedriver.exe";
	
	static ArrayList<String> listManyLinks = new ArrayList<String>();
	static String oneLink = "";
	
	static WebDriver driver;
	static WebDriverWait wait;
	
	static String username;
	static String password;
	static int number_videos_create = 100;
	static int number_videos_into_one = 1;
	static String name_prefix = "";
	static int index_name_from = 1;
	
	static void init() throws IOException, InterruptedException
	{
		// read one link
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_one_link)));
			String line = br.readLine();
			if(line == null)
			{
				System.out.println("No link found in " + file_one_link);
				System.exit(1);
			} else {
				oneLink = line;
			}
			br.close();
		}
		
		// read many links
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_many_links)));
			String line;
			while((line=br.readLine())!=null)
			{
				line = line.trim();
				listManyLinks.add(line);
			}
			br.close();
			if(listManyLinks.size()==0)
			{
				System.out.println("No link found in " + file_many_links);
			}
		}
		
		// login
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Parameters.file_info)));
			String line;
			while ((line = br.readLine()) != null) {
				String[] tempArray = line.split("=");
				if (tempArray[0].trim().compareToIgnoreCase("username") == 0) {
					username = tempArray[1].trim();
					System.out.println("username = " + username);
				} else if (tempArray[0].trim().compareToIgnoreCase("password") == 0) {
					password = tempArray[1].trim();
				} else if (tempArray[0].trim().compareToIgnoreCase("number_videos_create") == 0) {
					number_videos_create = Integer.parseInt(tempArray[1].trim());
					System.out.println("number_videos_create = " + number_videos_create);
				} else if (tempArray[0].trim().compareToIgnoreCase("number_videos_into_one") == 0) {
					number_videos_into_one = Integer.parseInt(tempArray[1].trim());
					System.out.println("number_videos_into_one = " + number_videos_into_one);
				} else if (tempArray[0].trim().compareToIgnoreCase("name_prefix") == 0) {
					name_prefix = tempArray[1].trim();
					System.out.println("name_prefix = " + name_prefix);
				} else if (tempArray[0].trim().compareToIgnoreCase("index_name_from") == 0) {
					index_name_from = Integer.parseInt(tempArray[1].trim());
					System.out.println("index_name_from = " + index_name_from);
				}
			}
			br.close();
		}
		
		// driver
		System.setProperty("webdriver.chrome.driver", Parameters.file_driver);
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(100, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, 100);

		// login
		driver.get("https://accounts.google.com/ServiceLogin?passive=true&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26app%3Ddesktop%26feature%3Dsign_in_button%26next%3D%252F%26hl%3Den&service=youtube&uilel=3&hl=en#identifier");
		driver.findElement(By.id("Email")).sendKeys(username);
		driver.findElement(By.id("next")).click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Passwd")));

		driver.findElement(By.id("Passwd")).sendKeys(password);
		driver.findElement(By.id("signIn")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yt-picker-language-button")));
		
		// change language --> English (in case it was not in English)
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yt-picker-language-button")));
		Thread.sleep(1000);
		driver.findElement(By.id("yt-picker-language-button")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//strong[@class=\"yt-picker-item\"]")));
		System.out.println(driver.findElement(By.xpath("//strong[@class=\"yt-picker-item\"]")).getText());
		Thread.sleep(1000);
		if(driver.findElement(By.xpath("//strong[@class=\"yt-picker-item\"]")).getText().compareTo("English (US)")!=0)
		{
			driver.findElement(By.xpath("//button[@value=\"en\"]")).click();
		}
		// wait until change done
		while(true)
		{
			System.out.println("Wait until change to English done....");
			if(driver.findElement(By.xpath("//link[@rel=\"search\"]")).getAttribute("href").toString().contains("locale=en_US"))
			{
				break;
			}
			Thread.sleep(500);
		}
		
		System.out.println("Done init!");
	}
	
	static void uploadVideos(ArrayList<String> listVideos) throws InterruptedException
	{
		driver.get("https://www.youtube.com/editor?feature=upload");
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cc-tab")));
		
		String video_title = name_prefix + " #" + index_name_from;
		driver.findElement(By.id("video-title")).clear();
		Thread.sleep(200);
		driver.findElement(By.id("video-title")).sendKeys(video_title);
		Thread.sleep(200);
		driver.findElement(By.id("cc-tab")).click();

		for(int i=0; i<listVideos.size(); i++)
		{
			System.out.println(listVideos.get(i));
			
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"cc-search-input\"]/form/span[1]/input")));
			driver.findElement(By.xpath("//*[@id=\"cc-search-input\"]/form/span[1]/input")).clear();
			Thread.sleep(200);
			driver.findElement(By.xpath("//*[@id=\"cc-search-input\"]/form/span[1]/input")).sendKeys(listVideos.get(i));
			Thread.sleep(200);
			driver.findElement(By.xpath("//*[@id=\"cc-search-input\"]/form/span[1]/input")).sendKeys(Keys.ENTER);

			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"" + "cc-thumb-" + listVideos.get(i).replace("https://www.youtube.com/watch?v=", "") + "\"]")));
			driver.findElement(By.xpath("//*[@id=\"" + "cc-thumb-" + listVideos.get(i).replace("https://www.youtube.com/watch?v=", "") + "\"]")).click();		   
			
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"" + "cc-thumb-" + listVideos.get(i).replace("https://www.youtube.com/watch?v=", "") + "\"]/div/span/button[2]")));
			driver.findElement(By.xpath("//*[@id=\"" + "cc-thumb-" + listVideos.get(i).replace("https://www.youtube.com/watch?v=", "") + "\"]/div/span/button[2]")).click();
			Thread.sleep(1000);
			while(true)
			{
				System.out.println("waiting add video done ...");
				if(driver.findElement(By.id("save-changes-message")).getText().compareToIgnoreCase("All changes saved.")==0)
				{
					break;
				} Thread.sleep(100);
			}
		}
		
		driver.findElement(By.id("publish-button")).click();
		// wait create new video done
		while(true)
		{
			System.out.println("wait create new video done ....");
			if(driver.getCurrentUrl().toString().contains("https://www.youtube.com/watch"))
			{
				break;
			}
			Thread.sleep(500);
		}
	}
	
	public static void main(String [] args) throws IOException, InterruptedException
	{
		init();
		for(int i=0; i<number_videos_create; i++)
		{
			ArrayList<Integer> listIndexs = Utils.getListRandomNumbers(number_videos_into_one, listManyLinks.size());
			ArrayList<String> listVideos = new ArrayList<>();
			for(int index=0; index<listIndexs.size(); index++)
			{
				listVideos.add(listManyLinks.get(listIndexs.get(index)));
			}
			listVideos.add(oneLink);
			System.out.println(listVideos);
			uploadVideos(listVideos);
			index_name_from++;
		}
		
		driver.get("https://www.youtube.com/my_videos?o=U");
	}
}
