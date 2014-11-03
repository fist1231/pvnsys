package com.pvnsys.ttts.tttsGwtClient.server.stocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.pvnsys.ttts.tttsGwtClient.server.CustomRequestFactoryServlet;
import com.pvnsys.ttts.tttsGwtClient.server.authentication.UserAuthenticationProvider;
import com.pvnsys.ttts.tttsGwtClient.server.utils.DBConnection;
import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;
import com.pvnsys.ttts.tttsGwtClient.shared.dto.ChartsDTO;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.TAUtil;

/**
 * The server-side implementation of the RPC service.
 */
public class StockServiceImpl {

	public List<StockVO> searchStocks(String input) throws IllegalArgumentException {
		
		
		List<StockVO> stocks = new ArrayList<StockVO>();
		// Verify that the input is valid. 
//		if (!FieldVerifier.isValidName(input)) {
//			// If the input is not valid, throw an IllegalArgumentException back to
//			// the client.
//			throw new IllegalArgumentException(
//					"Name must be at least one character long");
//		}
		
/*		
		String query = "SELECT id, name, open, close, high, low, agj_close, volume, stock_date FROM stock WHERE name like ? " +
				" and stock_date=(select max(stock_date) from stock where name like ?) order by name";

		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		    conn = DBConnection.getConnection();
		    stmt = conn.prepareCall(query);
		    stmt.setString(1, input.trim() + "%");
		    stmt.setString(2, input.trim() + "%");
		    
		    rs = stmt.executeQuery();
		    while (rs.next()) {
		        Long id = rs.getLong("id");
		        String name = rs.getString("name");
		        Double open = rs.getDouble("open");
		        Double close = rs.getDouble("close");
		        Double high = rs.getDouble("high");
		        Double low = rs.getDouble("low");
		        Double adjClose = rs.getDouble("agj_close");
		        Long volume = rs.getLong("volume");
		        Date stockDate = rs.getDate("stock_date");
		        stocks.add(new StockVO(id, name, open, close, high, low, adjClose, stockDate, volume));
		    }
		} catch(SQLException e) {
		        System.err.println("Mysql Statement Error: " + query);
		        e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    try {
		    	if(stmt != null) {
		    		stmt.close();
		    		stmt = null;
		    	}
		    	if(rs != null) {
		    		rs.close();
		    		rs = null;
		    	}
		    	if(conn != null) {
		    		conn.close();
		    		conn = null;
		    	}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		Log log = LogFactory.getLog(StockServiceImpl.class);
//		log.debug("**888***************** Hello log");
		System.out.println("*********** Server trace for: " + input);
*/
		return stocks;
	}

	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	
	public StockVO getLastStockQuote(String input) throws IllegalArgumentException {
		
		StockVO stockVO = null;
/*		
		String query = "SELECT id, name, open, close, high, low, agj_close, volume, stock_date FROM stock WHERE name = ?" +
		" and stock_date=(select max(stock_date) from stock where name = ?)";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		    conn = DBConnection.getConnection();
		    stmt = conn.prepareCall(query);
		    stmt.setString(1, input);
		    stmt.setString(2, input);
		    
		    rs = stmt.executeQuery();
		    while (rs.next()) {
		        Long id = rs.getLong("id");
		        String name = rs.getString("name");
		        Double open = rs.getDouble("open");
		        Double close = rs.getDouble("close");
		        Double high = rs.getDouble("high");
		        Double low = rs.getDouble("low");
		        Double adjClose = rs.getDouble("agj_close");
		        Long volume = rs.getLong("volume");
		        Date stockDate = rs.getDate("stock_date");
		        stockVO = new StockVO(id, name, open, close, high, low, adjClose, stockDate, volume);
		    }
		} catch(SQLException e) {
		        System.err.println("Mysql Statement Error: " + query);
		        e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    try {
		    	if(stmt != null) {
		    		stmt.close();
		    		stmt = null;
		    	}
		    	if(rs != null) {
		    		rs.close();
		    		rs = null;
		    	}
		    	if(conn != null) {
		    		conn.close();
		    		conn = null;
		    	}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/		
		
		// Uncomment if database is installed
		stockVO = new StockVO(1l, "SPY", 180.01, 182.12, 182.69, 179.93, 182.12, new Date(), 120123445l);
		return stockVO;
	}

	
	
	public List<String> searchAllStockNames(String input) {
		
		
		List<String> names = new ArrayList<String>();
/*
		// Verify that the input is valid. 
		String query = "SELECT distinct name FROM stock where UPPER(name) like ? order by name";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		    conn = DBConnection.getConnection();
		    stmt = conn.prepareCall(query);
		    stmt.setString(1, input.trim().toUpperCase() + "%");
		    
		    rs = stmt.executeQuery();
		    while (rs.next()) {
		        String name = rs.getString("name");
		        names.add(name);
		    }
		} catch(SQLException e) {
		        System.err.println("Mysql Statement Error: " + query);
		        e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    try {
		    	if(stmt != null) {
		    		stmt.close();
		    		stmt = null;
		    	}
		    	if(rs != null) {
		    		rs.close();
		    		rs = null;
		    	}
		    	if(conn != null) {
		    		conn.close();
		    		conn = null;
		    	}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		Log log = LogFactory.getLog(StockServiceImpl.class);
//		log.debug("**888***************** Hello log");
		System.out.println("########## Suggest trace for: " + input);
*/
		return names;
	}
	
	public String authenticate(String username, String password) {
		User user = new User(username, password, true, true, true, true, new ArrayList<GrantedAuthority>());
		Authentication auth = new UsernamePasswordAuthenticationToken(user, password, new ArrayList<GrantedAuthority>());
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(CustomRequestFactoryServlet.getThreadLocalServletContext());
		UserAuthenticationProvider authenticationProvider = context.getBean(UserAuthenticationProvider.class);
		try {
		    auth = authenticationProvider.authenticate(auth);
		} catch (BadCredentialsException ex) {
		    throw new AuthenticationServiceException(ex.getMessage(), ex);
		}
		SecurityContext sc = new SecurityContextImpl();
		sc.setAuthentication(auth);

		SecurityContextHolder.setContext(sc);
		
		return "success";
	}

	public List<StockVO> searchStockByName(String idx) throws IllegalArgumentException {
		
		
		List<StockVO> stocks = new ArrayList<StockVO>();
/*		
		String query = "SELECT id, name, open, close, high, low, agj_close, volume, stock_date FROM stock WHERE name = ? " +
				" order by stock_date";

		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		    conn = DBConnection.getConnection();
		    stmt = conn.prepareCall(query);
		    stmt.setString(1, idx.trim());
		    
		    rs = stmt.executeQuery();
		    while (rs.next()) {
		        Long id = rs.getLong("id");
		        String name = rs.getString("name");
		        Double open = rs.getDouble("open");
		        Double close = rs.getDouble("close");
		        Double high = rs.getDouble("high");
		        Double low = rs.getDouble("low");
		        Double adjClose = rs.getDouble("agj_close");
		        Long volume = rs.getLong("volume");
		        Date stockDate = rs.getDate("stock_date");
		        stocks.add(new StockVO(id, name, open, close, high, low, adjClose, stockDate, volume));
		    }
		} catch(SQLException e) {
		        System.err.println("Mysql Statement Error: " + query);
		        e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    try {
		    	if(stmt != null) {
		    		stmt.close();
		    		stmt = null;
		    	}
		    	if(rs != null) {
		    		rs.close();
		    		rs = null;
		    	}
		    	if(conn != null) {
		    		conn.close();
		    		conn = null;
		    	}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		Log log = LogFactory.getLog(StockServiceImpl.class);
//		log.debug("**888***************** Hello log");
		System.out.println("%%%%%%%%%%%% Server trace for: " + idx);
*/
		return stocks;
	}

	
	public ChartsDTO getChartsData(String name) throws IllegalArgumentException {
		
		ChartsDTO chartsDto = new ChartsDTO();
		chartsDto.setData(searchStockByName(name));
		
		Double[] close = new Double[chartsDto.getData().size()];
		int counter = 0;
		for(StockVO stockVO: chartsDto.getData()) {
			close[counter] = new Double(stockVO.getClose());
			counter++;
		}
		
		MacdVO macdVO = getMacd(close);
		chartsDto.setMacd(macdVO);
		
		return chartsDto;
	}	
	
	private MacdVO getMacd(Double[] close) {
		return TAUtil.getMacd(close);
	}

	
	
}
