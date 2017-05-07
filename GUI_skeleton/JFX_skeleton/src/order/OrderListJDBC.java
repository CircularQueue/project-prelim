package order;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * OrderListJDBC
 * Used for implementing the database connection to order list.  It will connect to RAS database.  The OrderListJDBC class will be a
 * mirror class of the OrderList class.
 * @author benjaminxerri
 *
 */
public class OrderListJDBC  {

		private HashMap<Integer,Order> orders;
		private HashMap<Integer,Order> allOrders;
		private HashMap<Integer, List<OrderItems>> orderItems;
		
	   // JDBC driver name and database URL
	   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost/RAS3";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "mcNp#tzpQi7";
	   
	   static Connection conn = null; //hold connection
	/**Constructor for OrderList JDBC
	 * The connection is created here
	 * 
	 */
	public OrderListJDBC() {
		allOrders = new HashMap<Integer,Order>();
		orders = new HashMap<Integer,Order>();
		orderItems = new HashMap<Integer, List<OrderItems>>();
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
	 	String insertOrderItems = "INSERT INTO Order_Items (orderId,Seat_Number,Menu_Item_Id,item_name,item_price,item_description)"
	 			+ " VALUES (?,?,?,?,?,?)";
		PreparedStatement stmt = null;
	 	try { 
	 		
	 		stmt = conn.prepareStatement(insertTableSQL);
	 		
	 		stmt.setInt(1, o.getOrderId()); //randomly generated
	 		stmt.setInt(2, o.getServerId()); //who logs in
	 		stmt.setInt(3, o.getTableIdinOrder()); //what table they click
	 		stmt.setInt(4, o.getOrderStatus()); //0 to start automatically
	 		stmt.setDouble(5, o.getOrderTotal()); //starts at 0, then generated by calculated item_id and updated
	 		stmt.execute();
	 		//orders.put(o.getOrderId(), o);
	 		System.out.println("Order Added");
	 		
	 	}catch(Exception e){
	 		System.err.println(e);
	 		return false;
	 	}
	 	for(int i =0; i < args.length;i++){
			try {
		 		stmt = conn.prepareStatement(insertOrderItems);
		 		stmt.setInt(1, o.getOrderId()); //randomly generated
		  //who logs in
		 		stmt.setInt(2, args[i].getSeatNumber());
				stmt.setInt(3, args[i].getID());
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
	 	populateUncookedOrders();
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
		
		Order searchOrder = searchOrder(orderId);
		if(searchOrder==null)
		{
			//System.out.println("Order not found");
			return null;
		}
		else
			
		try
		{
			
			String getTotal = " select sum(item_price) from order_items where orderid =" + orderId;
			PreparedStatement myStmt1 = conn.prepareStatement(getTotal);
			ResultSet result = myStmt1.executeQuery();
		     result.next();
		     String sum = result.getString(1);
		     //System.out.println("The Sum is = "+sum);
		     double temp = Double.parseDouble(sum);
		     double sum1 = Math.round(temp*100);
		     sum1= sum1/100;
			
			//System.out.println("Inside payOrder Begining");
		String update = "UPDATE orders set orderstatus=?, ordertotal = ? where orderid =?";
		PreparedStatement myStmt = conn.prepareStatement(update);
		myStmt.setInt(1,2);
		myStmt.setDouble(2,sum1);
		myStmt.setInt(3,orderId);
		myStmt.executeUpdate();
		deleteOrderItem(orderId); //deletes items in order items;
		//order total generate.
		/*
		 * count total for order id on the itemPrice column. 
		 */
		
		//System.out.println("in payorder in JDBC");
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
	public Order deleteOrder(int orderID){
		Order searchOrder = searchOrder(orderID);
		if(searchOrder==null)
		{
			//System.out.println("Order not found");
			return null;
		}
		else
		{
			try
			{
				deleteOrderItem(orderID);
				System.out.print("Items were deleted");
				String deleteOrders = "DELETE from orders where orderid = " +orderID;
				PreparedStatement delete1 = conn.prepareStatement(deleteOrders); 
				delete1.executeUpdate();
				// Delete from order_items as well. 	
			}
			catch(Exception e)
			{
		 		System.err.println(e);
		 	}
		}
		
		return searchOrder;
		
	}
	
	private Order deleteOrderItem(int orderID)
	{
		Order searchOrder = searchOrder(orderID);
		if(searchOrder==null)
		{
			//System.out.println("Order not found");
			return null;
		}
		else
			
		try
		{
			String deleteOrderItems = "DELETE from order_items where orderid = " +orderID;
			PreparedStatement delete2 = conn.prepareStatement(deleteOrderItems); 
			delete2.executeUpdate();
		}
		catch(Exception e)
		{
	 		System.err.println(e);
	 	}
		return searchOrder;
	}
	
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
				//System.out.println("in search ordeer");
			String searchOrder = "Select orderid, serverid, tableid, orderstatus, ordertotal from Orders where orderid ="+orderId;
			PreparedStatement myStmt = conn.prepareStatement(searchOrder);
//			myStmt.setInt(1,orderId);
			ResultSet myRs = myStmt.executeQuery(searchOrder);
			
				while(myRs.next()){
					// I should take the results and put it in order object. 
				orderId1 = Integer.valueOf(myRs.getString("orderid"));
				serverId= Integer.valueOf(myRs.getString("serverid"));
				tableId = Integer.valueOf(myRs.getString("tableid"));
				orderStatus = Integer.valueOf(myRs.getString("orderstatus"));
				orderTotal = Double.parseDouble(myRs.getString("ordertotal"));
				}
			}
			catch(Exception e)
			{
		 		System.err.println(e);
		 		
		 	}

			if(orderId1!=orderId)
			{
				return null; 
			}
			else
			{
				Order orderDetails = new Order (orderId1, serverId, tableId, orderStatus, orderTotal);
			return orderDetails;
			}
			
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
		   String orders = "SELECT Orders.OrderID, Menu_Item_Id,item_name,item_price,item_description " + 
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
		         System.out.print(", Item: " + itemName);
		         System.out.print(", Price: " + itemPrice);
		         System.out.println(",Desc: " + itemDesc);
		      
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean populateAllOrders() {
		
		   Statement stmt = null;
		   int id = 0;
		   String uncookedOrds = "SELECT * FROM Orders";
			
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
						allOrders.put(id, ord);
						populateUncookedOrderItems(id);
					//	System.out.println("Order " + id + " was placed into hashmap");	
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				
			 System.out.println("Populated ALL Orders");
			 return true;
		
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
					//	System.out.println("Order " + id + " was placed into hashmap");	
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
				
			 System.out.println("Populated Orders");
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
						int orderItemId = rs.getInt("Menu_Item_Id");
						String itemName = rs.getString("item_name");
						double price = rs.getDouble("item_price");
						String itemDesc = rs.getString("item_description");

						
						OrderItems ord = new OrderItems(id,seatNum,orderItemId,itemName,price,itemDesc);
						ListOfItems.add(ord);
						//System.out.println("OrderItems " + id + " was placed into hashmap");
		
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return false;
				}
			 	
				orderItems.put(id,ListOfItems);
				 System.out.println("Populated Orders- Items");

				return true;
	}
	
	public HashMap<Integer, Order> getOrder(){
		populateAllOrders();
		return allOrders;
	
	}
	
	public HashMap<Integer, List<OrderItems>> getOrderItems(){
		return orderItems;
	}
	
	public boolean changeOrderStatus(int orderId, int newStatus){
		String cook = "UPDATE orders set orderstatus=? where orderid =?";
	 		
		PreparedStatement stmt = null;
	 	try { 
	 		
	 		stmt = conn.prepareStatement(cook);
	 		
	 		stmt.setInt(1, newStatus);
	 		stmt.setInt(2, orderId);
	 		stmt.execute();
	 		System.out.println("Order was cooked");
	 		
	 	}catch(Exception e){
	 		System.err.println(e);
	 		return false;
	 	}
	 	populateUncookedOrders();
		return true;
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
	
	
	public static Connection getDB() {
		   
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



