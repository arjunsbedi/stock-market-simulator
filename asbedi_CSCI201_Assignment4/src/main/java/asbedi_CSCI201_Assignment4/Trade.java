package asbedi_CSCI201_Assignment4;

public class Trade {
    public int tradeID;
    public int userID;
    public String ticker;
    public int num;
    public double price;

    // Constructor
    public Trade(int tradeID, int userID, String ticker, int num, double price) {
        this.tradeID = tradeID;
        this.userID = userID;
        this.ticker = ticker;
        this.num = num;
        this.price = price;
    }

	public Trade() {
		// TODO Auto-generated constructor stub
	}

}
