package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;
	   
	   public MySQLConnection() {
	  	 try {
	  		 Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
	  		 conn = DriverManager.getConnection(MySQLDBUtil.URL);
	  		
	  	 } catch (Exception e) {
	  		 e.printStackTrace();
	  	 }
	   }


	@Override
	public void close() {
		 if (conn != null) {
	  		 try {
	  			 conn.close();
	  		 } catch (Exception e) {
	  			 e.printStackTrace();
	  		 }
	  	 }
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return;
		}
		
		try {
			
			 String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, userId);
	  		 for(String itemId : itemIds) {
	  			 ps.setString(2, itemId);
	  			 ps.execute();
	  		 }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return;
		}
		
		try {
			 String sql = "DELETE FROM history WHERE user_id = ? and item_id = ?";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, userId);
	  		 for(String itemId : itemIds) {
	  			 ps.setString(2, itemId);
	  			 ps.execute();
	  		 }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> itemIds = new HashSet<>();
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return itemIds;
		}
		try {
			 String sql = "SELECT item_id FROM history WHERE user_id = ? ";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, userId);
	  		 ResultSet rs = ps.executeQuery();
	  		 while (rs.next()) {
				String itemId = rs.getString("item_id");
				itemIds.add(itemId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemIds ;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<Item> items = new HashSet<>();
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return items;
		}
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			 String sql = "SELECT * FROM items WHERE item_id = ? ";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 for(String item_id : itemIds) {
	  			 
	  			 ps.setString(1, item_id);
	  			 ResultSet rs = ps.executeQuery();
	  			 ItemBuilder builder = new ItemBuilder();
	  			 
	  			 while (rs.next()) {
	  				builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(item_id));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));
					
					items.add(builder.build());
	  			 }			 
	  		 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return items;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<>();
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return categories;
		}
		
		try {
			String sql = "SELECT category from categories WHERE item_id = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String category = rs.getString("category");
				categories.add(category);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
	
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return;
		}
		
		try {
			 String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, item.getItemId());
	  		 ps.setString(2, item.getName());
	  		 ps.setDouble(3, item.getRating());
	  		 ps.setString(4, item.getAddress());
	  		 ps.setString(5, item.getImageUrl());
	  		 ps.setString(6, item.getUrl());
	  		 ps.setDouble(7, item.getDistance());
	  		 ps.execute();
	  		 
	  		 sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
	  		 ps = conn.prepareStatement(sql);
	  		 ps.setString(1, item.getItemId());
	  		 for(String category : item.getCategories()) {
	  			 ps.setString(2, category);
	  			 ps.execute();
	  		 }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return"";
		}
		String name = "";
		try {
			 String sql = "SELECT first_name, last_name  FROM users where user_id =  ?";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, userId);
	  		 ResultSet rs = ps.executeQuery();
	  		 while (rs.next()) {
	  			 name = rs.getString("first_name") + rs.getString("last_name");
	  		 }
	  		 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
	  		   System.err.println("DB connection failed");
	  		   return false;
		}
		try {
			 String sql = "SELECT user_id  FROM users WHERE user_id =  ? and password = ?";
	  		 PreparedStatement ps = conn.prepareStatement(sql);
	  		 ps.setString(1, userId);
	  		 ps.setString(2, password);
	  		 ResultSet rs = ps.executeQuery();
	  		 if (rs.next()) {
	  			 return true;
	  		 }
	  		 
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}

		try {
			String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			
			return ps.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}


}
