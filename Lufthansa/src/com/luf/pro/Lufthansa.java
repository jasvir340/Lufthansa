package com.luf.pro;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Lufthansa { 

	private static String CLIENT_ID = null;
	private static String CLIENT_SECRET = null;
	private static final String GRANT_TYPE = "client_credentials";
	private static final String TOKEN_ACCESS_URL = "https://api.lufthansa.com/v1/oauth/token";
	private static final String LUFTANSA_BASE_URL = "https://api.lufthansa.com/v1/";
	private static String ACCESS_TOKEN = null;
	
	private static HttpGet httpGet = null;
	private static HttpPost httpPost = null;
	private static HttpResponse httpResponse = null;
	private static HttpClient httpClient = HttpClientBuilder.create().build();
	
	private static ArrayList<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
	
	private static BufferedReader br = null;
		
	private static String convertResponseToString(InputStream is) throws IOException
	{
		StringBuilder data = new StringBuilder();
		br = new BufferedReader(new InputStreamReader(is));
		String temp = null;
		while((temp=br.readLine())!=null)
		{
			data.append(temp);
		}
		br.close();
		return data.toString();
	}
	
	private static String findNearestAirport(double latitude, double longitude) throws ClientProtocolException, IOException
	{
		
		String url = LUFTANSA_BASE_URL+"references/airports/nearest/"+latitude+","+longitude;
		httpGet = new HttpGet(url);
	
		httpGet.setHeader("Accept","application/json");
		httpGet.setHeader("Authorization","Bearer "+ACCESS_TOKEN);
		
		httpResponse = httpClient.execute(httpGet);
		if(httpResponse.getEntity().getContent()!=null)
		{
			data = convertResponseToString(httpResponse.getEntity().getContent());
			clearResponse();
		}
		return data;
	}
	
	private static void clearResponse() throws UnsupportedOperationException, IOException
	{
		
		httpResponse.getEntity().getContent().close();
	}
	
	private static void initializeCredentials(String filePath) throws IOException
	{
		br = new BufferedReader(new FileReader(new File(filePath)));
		String credentials[] = br.readLine().split(",");
		CLIENT_ID = credentials[0];
		CLIENT_SECRET = credentials[1];
		br.close();
	}
	
	private static String data = null;
	
	public static void main(String[] args) throws ClientProtocolException, IOException, ParseException {
		
		double latitude = 0, longitude = 0;
		
		Scanner sc = new Scanner(System.in);
		
		initializeCredentials(args[0]);
		
		httpPost = new HttpPost(TOKEN_ACCESS_URL);
		
		parameters.add(new BasicNameValuePair("client_id", CLIENT_ID));
		parameters.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
		parameters.add(new BasicNameValuePair("grant_type", GRANT_TYPE));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			System.out.println("Something went wrong !!!");
			e.printStackTrace();
		}
		
		try {
			httpResponse = httpClient.execute(httpPost);
			if(httpResponse.getEntity().getContent()!=null)
			{
				data = convertResponseToString(httpResponse.getEntity().getContent());
				clearResponse();
				ACCESS_TOKEN = data.split(",")[0].split(":")[1].replaceAll("\"", "");
			}
		} catch (IOException e) {
			System.out.println("Something went wrong while executing request");
			e.printStackTrace();
		}
				
		System.out.println("Please enter your coordinates ...");
		System.out.print("Latitude: ");
		latitude = sc.nextDouble();
		System.out.print("Longitude: ");
		longitude = sc.nextDouble();
	
		JSONObject nearestAirport = (JSONObject) new JSONParser().parse(findNearestAirport(latitude, longitude));

		nearestAirport = (JSONObject) ((JSONObject) nearestAirport.get("NearestAirportResource")).get("Airports");
		JSONArray airports = (JSONArray) nearestAirport.get("Airport");
		String city = "";
		
		for(int i=0;i<1;i++)
		{
			nearestAirport = (JSONObject)airports.get(i);
			city = ((JSONObject)((JSONArray)((JSONObject)nearestAirport.get("Names")).get("Name")).get(0)).get("$").toString();
			System.out.println("City Code: "+nearestAirport.get("CityCode")+"\tCity: "+city+"\tDistance: "+((JSONObject)nearestAirport.get("Distance")).get("Value")+" KM");
		}
		
		sc.close();
	}
}
