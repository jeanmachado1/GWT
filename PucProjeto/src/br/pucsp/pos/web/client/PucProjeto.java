package br.pucsp.pos.web.client;

import java.util.ArrayList;
import java.util.Timer;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PucProjeto implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable stocksFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSimbolTextoBox = new TextBox();
	private Button addStockButton = new Button("Add");
	private Label lastUpdatelLabel = new Label();
	private ArrayList<String> stocks = new ArrayList<String>();
	
	private static final int REFRESH_INTERVAL = 5000;
	
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		//Create table  for stock data
		stocksFlexTable.setText(0, 0, "Symbol");
		stocksFlexTable.setText(0, 1, "Price");
		stocksFlexTable.setText(0, 2, "Change");
		stocksFlexTable.setText(0, 3, "Remove");
		
		//Assemble Add stock pane
		addPanel.add(newSimbolTextoBox);
		addPanel.add(addStockButton);
		
		//Assemble main menu
		mainPanel.add(stocksFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(lastUpdatelLabel);
		
		//Associate the Main panel with the HTML host page
		RootPanel.get("pucProjeto").add(mainPanel);
		
		//Move cursor focus to the input box
		newSimbolTextoBox.setFocus(true);
		
		// Setup timer to refresh list automatically
		com.google.gwt.user.client.Timer refreshTimer = new com.google.gwt.user.client.Timer(){
			public void run(){
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		//Listen for mouse events on the Add button
		addStockButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				addStock();
				
			}
		});
		
		newSimbolTextoBox.addKeyDownHandler( new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
					addStock();
				}
				
			}
		});		
	}
	private void addStock(){
		final String symbol = newSimbolTextoBox.getText().toUpperCase().trim();
		newSimbolTextoBox.setFocus(true);
		
		// Stock code must  be between 1 and 10 chars that are numbers, letters, or dots.
		if(!symbol.matches("^[0-9A-Z&#92;&#92;.]{1,10}$")){
			com.google.gwt.user.client.Window.alert("'"+symbol+"'is not a valid symbol.");
			newSimbolTextoBox.selectAll();
			return;
		}
		int row = stocksFlexTable.getRowCount();
		stocks.add(symbol);
		stocksFlexTable.setText(row, 0, symbol);
		// add Button to remove this stock from table
		Button removeStockButton = new Button("X");
		removeStockButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int removedIndex = stocks.indexOf(symbol);
				stocks.remove(removedIndex);
				stocksFlexTable.removeRow(removedIndex +1);
				
			}
		});
		stocksFlexTable.setWidget(row, 3, removeStockButton);
		refreshWatchList();
	}
	private void refreshWatchList(){
		final double MAX_PRICE = 100.0;// U$ 100
		final double MAX_PRICE_CHANGE = 0.02;// +/- 2%
		
		StockPrice[] prices = new StockPrice[stocks.size()];
		for (int i = 0; i < stocks.size(); i++) {
			double price = Random.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE * (Random.nextDouble()*2.0 -1.0);
			
			prices[i]= new StockPrice(stocks.get(i),price,change);
		}
		updateTable(prices);
	}
	
	private void updateTable(StockPrice[] prices){
		for (int i = 0; i < prices.length; i++) {
			updateTable(prices[i]);
		}
		// Display timestamp showing last refresh.
	      DateTimeFormat dateFormat = DateTimeFormat.getFormat(
	      DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
	      lastUpdatelLabel.setText("Last update : " + dateFormat.format(new Date()));
	}
	private void updateTable(StockPrice price){
		if(!stocks.contains(price.getSymbol())){
			return;
		}
		
		int row = stocks.indexOf(price.getSymbol())+1;
		
		//Format the data in the price and change fields
		String priceText = NumberFormat.getFormat("#,##0.00").format(price.getPrice());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(price.getChange());
		String changePercText = changeFormat.format(price.getChange());//TODO Criar get e set
		
		// populate  the price and Change fields with  new data
		stocksFlexTable.setText(row,1,priceText);
		stocksFlexTable.setText(row,2,changeText + "("+changePercText+"%)");
		
		
		
	}
	
}
