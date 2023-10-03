package it.cavemc.cavemc;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FriendDB {
    public Connection connection;

    public FriendDB(String file) throws SQLException, ClassNotFoundException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:"+file);
        Class.forName("org.sqlite.JDBC");
        createTable();
        createTableFriends();
        System.out.println("DbAmici connesso!");
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS amici(player_1 TEXT NOT NULL, player_2 TEXT NOT NULL, data TEXT NOT NULL, stato TEXT NOT NULL)";
        Statement st = this.connection.createStatement();
        st.execute(sql);
    }

    private void createTableFriends() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS friends(player_1 TEXT NOT NULL, player_2 TEXT NOT NULL)";
        Statement st = this.connection.createStatement();
        st.execute(sql);
    }

    public int setFinallyFriend(String player_1, String player_2) {
        String querySetCave = "INSERT INTO friends(player_1, player_2) VALUES (?, ?)";
        int numRowsInserted = 0;
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(querySetCave);
            ps.setString(1, player_1);
            ps.setString(2, player_2);
            numRowsInserted = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
        return numRowsInserted;
    }


    public int setFriend(String player_1, String player_2, String date, String status) {
        String querySetCave = "INSERT INTO amici(player_1, player_2, data, stato) VALUES (?, ?, ?, ?)";
        int numRowsInserted = 0;
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(querySetCave);
            ps.setString(1, player_1);
            ps.setString(2, player_2);
            ps.setString(3, date);
            ps.setString(4, status);
            numRowsInserted = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
        return numRowsInserted;
    }

    public String getFriend(String playerName) throws SQLException {
        String query = "SELECT player_1, player_2 FROM friends WHERE player_1 = ? OR player_2 = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, playerName);
        st.setString(2, playerName);
        ResultSet rs = st.executeQuery();

        String friendName = null;

        while (rs.next()) {
            String player_1 = rs.getString("player_1");
            String player_2 = rs.getString("player_2");
            if (player_1.equals(playerName)) {
                friendName = player_2;
            } else {
                friendName = player_1;
            }
        }
        return friendName;
    }

    public List<String> getFriends(String playerName) throws SQLException {
        List<String> friendsList = new ArrayList<>();
        String query = "SELECT player_1, player_2 FROM friends WHERE player_1 = ? OR player_2 = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, playerName);
        st.setString(2, playerName);
        ResultSet rs = st.executeQuery();

        while (rs.next()) {
            String player_1 = rs.getString("player_1");
            String player_2 = rs.getString("player_2");
            if (player_1.equals(playerName)) {
                friendsList.add(player_2);
            } else {
                friendsList.add(player_1);
            }
        }
        return friendsList;
    }

    public List<String> getPlayer(String player2) {
        String query = "SELECT player_1 FROM amici WHERE stato = 'pending' AND player_2 = ?";
        List<String> pendingRequests = new ArrayList<>();
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(query);
            ps.setString(1, player2);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                pendingRequests.add(rs.getString("player_1"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pendingRequests;
    }

    public List<String> getStatus(String playerName) throws SQLException {
        String query = "SELECT stato FROM amici WHERE player_1 = ? OR player_2 = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, playerName);
        st.setString(2, playerName);
        ResultSet rs = st.executeQuery();

        List<String> status = new ArrayList<>();

        while (rs.next()) {
            String stato = rs.getString("stato");
            status.add(stato);
            break;
        }
        return status;
    }

    public void removefriends(String playerName) throws SQLException {
        String query = "DELETE FROM friends WHERE player_1 = ? OR player_2 = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, playerName);
        st.setString(2, playerName);
        st.executeUpdate();
    }

    public void removeRequest(String playerName) throws SQLException {
        String query = "DELETE FROM amici WHERE player_1 = ? OR player_2 = ?";
        PreparedStatement st = connection.prepareStatement(query);
        st.setString(1, playerName);
        st.setString(2, playerName);
        st.executeUpdate();
    }


    public void removeAllRequests() throws SQLException {
        String sql = "DELETE FROM amici";
        PreparedStatement st = connection.prepareStatement(sql);
        st.executeUpdate();
    }

    public void removeAllFriends() throws SQLException {
        String sql = "DELETE FROM friends";
        PreparedStatement st = connection.prepareStatement(sql);
        st.executeUpdate();
    }


    public int updateFriend(String player_1, String status) throws SQLException {
        String sql = "UPDATE amici set stato = ? WHERE player_1 = ?";
        int numRowsInserted = 0;
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, player_1);
            numRowsInserted = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
        return numRowsInserted;
    }

    public void closeConnection() {
        if (this.connection == null) {
            return;
        }
        try {
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.connection = null;
    }

    public static void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
