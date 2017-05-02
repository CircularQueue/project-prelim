package order;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ListChangeListener;

/**
 * OrderListJDBC
 * Used for implementing the database connection to order list.  It will connect to RAS database.  The OrderListJDBC class will be a
 * mirror class of the OrderList class.
 * @author benjaminxerri
 *
 */
public class OrderListJDBC  {

		private HashMap<Integer,Order> orders;
		private HashMap<Integer, List<OrderItems>> orderItems;
		private ObservableList<OrderItems> data;
		@FXML TableView<order.OrderItems> tableUser;
		@FXML TableColumn<order.OrderItems, Integer> orderIdCell;
		@FXML TableColumn<order.OrderItems, Integer> itemIdCell;
		@FXML TableColumn<order.OrderItems, Integer> seatNumberCell;
		@FXML TableColumn<order.OrderItems, String> itemNameCell;
		@FXML TableColumn<order.OrderItems, Double> itemPriceCell;
		@FXML TableColumn<order.OrderItems, String> itemDescriptionCell;
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost/RAS2";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "mcNp#tzpQi7";
	   
	  // private ObservableList<Order> data;
	   
	   static Connection conn = null; //hold connection
	/**Constructor for OrderList JDBC
	 * The connection is created here
	 * 
	 */
	public OrderListJDBC() {
		orders = new HashMap<Integer,Order>();
		orderItems = new HashMap<Integer, List<OrderItems>>();
        data = FXCollections.observableArrayList();
        conn = getDB();
		populateUncookedOrders();
	}
	/**
	 * JDBC method that handles the database logic of placing the order. It inserts the order into the 'Orders' table,
	 * Then it inserts any number of Order Items.
	 * @param o The order that will be added to the database.
	 * @return true of the order was placed, false otherwise
	 * @throws SQLException 
	 */
	public boolean placeOrder(Order o, OrderItems...args) throws SQLException{
		
	 	String insertTableSQL = "INSERT INTO Orders (orderId,serverId,tableId,orderStatus,orderTotal) VALUES (?,?,?,?,?)";
	 	String insertOrderItems = "INSERT INTO Order_Items (orderId,order_item_id,Seat_Number,item_name,item_price,item_description)"
	 			+ " VALUES (?,?,?,?,?,?)";
		PreparedStatement stmt = null;
	 	try { 
	 		
	 		stmt = conn.prepareStatement(insertTableSQL);
	 		
	 		stmt.setInt(1, o.getOrderId()); //randomly generated
	 		stmt.setInt(2, o.getServerIdInOrder()); //who logs in
	 		stmt.setInt(3, o.getTableIdinOrder()); //what table they click
	 		stmt.setInt(4, o.getOrderStatus()); //0 to start automatically
	 		stmt.setDouble(5, o.getOrderTotal()); //starts at 0, then generated by calculated item_id and updated
	 		stmt.execute();
	 		System.out.println("Order Added");
	 		
	 	}catch(Exception e){
	 		System.err.println(e);
	 		return false;
	 	}
	 	for(int i =0; i < args.length;i++){
			try {
		 		stmt = conn.prepareStatement(insertOrderItems);
		 		stmt.setInt(1, o.getOrderId()); //randomly generated
		 		stmt.setInt(2, args[i].getID()); //who logs in
		 		stmt.setInt(3, args[i].getSeatNumber());
		 		stmt.setString(4, args[i].getName()); //what table they click
		 		stmt.setDouble(5, args[i].getPrice()); //0 to start automatically
		 		stmt.setString(6, args[i].getDescription()); //starts at 0, then generated by calculated item_id and updated
		 		stmt.execute();
	 		System.out.println("Order Items Added");
			}catch(Exception e){
		 		System.err.println(e);
		 		return false;
		 	}
		}
		return true;
	}	

	/**
	 * Handles Database logic to update an existing order, already in the order list
	 * @param orderID,orderToChange Takes in an order that will be updated, and the new order that will replace it
	 * @return boolean Returns true for a successful update, false if the order was not updated.
	 */
	public boolean updateOrder(int orderID, Order orderToChange){return true;}
	
	/**
	 * Takes in order id to pay that specific order.
	 * communicated with order Entity to change the status of an order. 
	 * Method communicates with table entity to clear a table if the order status was successfully changed.
	 *  
	 * @param orderId Takes in the order ID
	 * @return Order Returns the newly updated order with a changed order status, or null if the order could not be updated.  
	 */
	
	public Order payOrder(int orderId){
		
		try
		{
			System.out.println("Inside payOrder Begining");
		String update = "UPDATE orders set orderstatus=? where orderid =?";
		PreparedStatement myStmt = conn.prepareStatement(update);
		myStmt.setInt(1,-88);
		myStmt.setInt(2,orderId);
		myStmt.executeUpdate();
		//Table.clearTable(0);
		System.out.println("in payorder in JDBC");
		}
		catch(Exception e)
		{
	 		System.err.println(e);
	 	}
		
		Order ordFound = searchOrder(orderId);
		return ordFound;
	
		}
	
	/**
	 * Handles Database logic to remove an order from the order table in the database.
	 * @param orderID The order id
	 * @return Order Returns the order object if it was deleted, or null if the order was not found.
	 */
	public Order deleteOrder(int orderID){return null;}
	
	/**
	 * 
	 * Handles Database logic to search if an order exists.
	 * @param orderId Search by order Id.
	 * @return Returns the order that was found, or null of the order was not found.
	 */
	public Order searchOrder(int orderId){
		
		
		int orderId1=0; int serverId=0; int tableId=0; int orderStatus=0; Double orderTotal=.0;
		// search order then get the order attributes.
		try
		{
			System.out.println("in search ordeer");
		String searchOrder = "Select orderid, serverid, tableid, orderstatus, ordertotal from Orders where orderid ="+orderId;
		PreparedStatement myStmt = conn.prepareStatement(searchOrder);
//		myStmt.setInt(1,orderId);
		ResultSet myRs = myStmt.executeQuery(searchOrder);
		
		while(myRs.next()){
				//System.out.println("The order id is" + myRs.getString("orderid") + "  order_item_id is:" + myRs.getString("Ordertotal") +" Server id is "+ myRs.getString("ServerId") );
				// I should take the results and put it in order object. 
			orderId1 = Integer.valueOf(myRs.getString("orderid"));
			serverId= Integer.valueOf(myRs.getString("serverid"));
			tableId = Integer.valueOf(myRs.getString("tableid"));
			orderStatus = Integer.valueOf(myRs.getString("orderstatus"));
			orderTotal = Double.parseDouble(myRs.getString("ordertotal"));
			//select orderid, serverid, tableid, orderstatus, ordertotal from orders;
			}
		}
		catch(Exception e)
		{
	 		System.err.println(e);
	 	}
		
		Order orderDetails = new Order (orderId1, serverId, tableId, orderStatus, orderTotal);
		return orderDetails;
	}
		
		
	
	
	/**
	 * Handles database logic to displays the Order List
	 * @return String Returns a string representation of the order.
	 */
	@Override public String toString(){
		
		String s ="";return s;
		
	}
	
	public void viewUncookedOrders(){
		   Statement stmt = null;
		   String orders = "SELECT Orders.OrderID, Order_Item_Id,item_name,item_price,item_description " + 
				   "FROM Orders JOIN Order_Items USING (OrderID) WHERE orderStatus = 0";
		 try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(orders);
			while(rs.next()){
				
				 int id  = rs.getInt("OrderID");
		         int itemID = rs.getInt("Menu_Item_Id");
		         String itemName = rs.getString("item_name");
		         String itemPrice = rs.getString("item_price");
		         String itemDesc = rs.getString("item_description");
	
		         System.out.print("ID: " + id);
		         System.out.print(", itemID: " + itemID);
		         System.out.print(", Name: " + itemName);
		         System.out.print(", Price: " + itemPrice);
		         System.out.println(",Desc: " + itemDesc);
		         
		         
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**private function that populates the orders hash table with orders.
	 * 
	 * @return
	 */
	private boolean populateUncookedOrders(){
		   Statement stmt = null;
		   int id = 0;
		   String uncookedOrds = "SELECT * FROM Orders WHERE orderStatus = 0";
			
			 try {
					stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(uncookedOrds);
					while(rs.next()){
						id = rs.getInt("OrderID");
						int servId = rs.getInt("ServerID");
						int table = rs.getInt("TableID");
						int status = rs.getInt("OrderStatus");
						double price = rs.getDouble("OrderTotal");
						
						Order ord = new Order(id,servId,table,status,price);
						orders.put(id, ord);
						populateUncookedOrderItems(id);
						System.out.println("Order " + id + " was placed into hashmap");	
					
						
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				
			 
			 return true;

	}
	
	private boolean populateUncookedOrderItems(int n) {
		ArrayList<OrderItems> ListOfItems = new ArrayList<OrderItems>();
		Statement stmt = null;
		 int id = 0;
		   String uncookedOrds = "SELECT * FROM Order_Items WHERE OrderID = " + n;
			
			 try {
					stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(uncookedOrds);
					while(rs.next()){
						id = n;
						
						int seatNum= rs.getInt("Seat_Number");
						int orderItemId = rs.getInt("Order_Item_Id");
						String itemName = rs.getString("item_name");
						double price = rs.getDouble("item_price");
						String itemDesc = rs.getString("item_description");

						
						OrderItems ord = new OrderItems(id,seatNum,orderItemId,itemName,price,itemDesc);
						ListOfItems.add(ord);
						
						data.add(ord);
						System.out.println("OrderItems " + id + " was placed into hashmap");
						/*
						orderIdCell.setCellValueFactory(new PropertyValueFactory<>("OrderID"));
						itemIdCell.setCellValueFactory(new PropertyValueFactory<>("Order_Item_Id"));
						seatNumberCell.setCellValueFactory(new PropertyValueFactory<>("Seat_Number"));
						itemNameCell.setCellValueFactory(new PropertyValueFactory<>("item_name"));
						itemPriceCell.setCellValueFactory(new PropertyValueFactory<>("item_price"));
						itemDescriptionCell.setCellValueFactory(new PropertyValueFactory<>("item_description"));
						
						tableUser.setItems(data);
						*/
		
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
			 	
				orderItems.put(id,ListOfItems);
		
				
				return true;
	}
	
	public void getOrder(){
		orders.forEach((k,v)->System.out.println(k + " " + v));
		orderItems.forEach((k,v)->System.out.println(k + " " + v));
	}
	
	public HashMap<Integer, List<OrderItems>> getOrderItems(){
		return orderItems;
	}
	
	
	public int getOrderIdByTable(int n){
		   Statement stmt = null;
		   int id = -1;
		   String orders = "SELECT OrderID FROM Orders WHERE TableID = " + n + " AND orderStatus = 0";
				   			
				  
		 try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(orders);
			while(rs.next()){
				
				  id = rs.getInt("OrderID");
		         System.out.print("OrderID: " + id);
		             
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		
		return id;
	}
	private static Connection getDB() {
		   
		   Connection db_connection = null;
		
		   try{
		      //STEP 2: Register JDBC driver
		      Class.forName(JDBC_DRIVER);

		   }
		   catch(ClassNotFoundException se){ //Catches if you don't have your JDBC driver
			      //Handle errors for JDBC
			      se.printStackTrace();
		   }
		   try {
			   db_connection = DriverManager.getConnection(DB_URL,USER,PASS); //get connection to Database
			   return db_connection;
		   }catch(SQLException e){
			   System.err.println(e.getMessage());
		   }
		   
		   return db_connection;
	   }
}



