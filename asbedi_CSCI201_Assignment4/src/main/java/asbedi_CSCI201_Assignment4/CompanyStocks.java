package asbedi_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CompanyStocks {
	public static void main(String [] args) {
		try {
			getProfile("AAPL");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static Stock getStock(String ticker) throws IOException {
		String apiKey = "cnt3ra1r01qi1jjgmfb0cnt3ra1r01qi1jjgmfbg";
        String endpoint = "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token=" + apiKey;
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder(); // Initialize StringBuilder to hold the entire response
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine); // Append each line to the StringBuilder
        }
        in.close();
        connection.disconnect();
        String jsonResponse = response.toString();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Stock stock = gson.fromJson(jsonResponse, Stock.class);
		System.out.println(jsonResponse); 
		return stock; 
	}
	
	static Profile getProfile(String ticker) throws IOException {
		String apiKey = "cnt3ra1r01qi1jjgmfb0cnt3ra1r01qi1jjgmfbg";
        String endpoint = "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token=" + apiKey;
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder(); // Initialize StringBuilder to hold the entire response
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine); // Append each line to the StringBuilder
        }
        in.close();
        connection.disconnect();
        String jsonResponse = response.toString();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Profile profile = gson.fromJson(jsonResponse, Profile.class);
		System.out.println(jsonResponse); 
		return profile; 
	}
	

}
