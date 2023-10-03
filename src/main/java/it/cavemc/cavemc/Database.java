package it.cavemc.cavemc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    public Connection connection;

    public Database(String file) throws SQLException, ClassNotFoundException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:"+file);
        Class.forName("org.sqlite.JDBC");
        createTable();
        createPrisonesTable();
        System.out.println("DbCaverne connesso!");
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS caves(name TEXT NOT NULL, x NUMERIC, y NUMERIC, z NUMERIC)";
        Statement st = this.connection.createStatement();
        st.execute(sql);
    }

    private void createPrisonesTable() throws SQLException {
        String table = "CREATE TABLE IF NOT EXISTS prisoners(player_name TEXT, x NUMERIC, y NUMERIC, z NUMERIC, world TEXT)";
        Statement st = this.connection.createStatement();
        st.execute(table);
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

    String querySetCave = "INSERT INTO caves(name, x, y, z) VALUES (?, ?, ?, ?)";
    public int setCave(String name, double x, double y, double z) {
        int numRowsInserted = 0;
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(querySetCave);
            ps.setString(1, name);
            ps.setDouble(2, x);
            ps.setDouble(3, y);
            ps.setDouble(4, z);
            numRowsInserted = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
        return numRowsInserted;
    }

    String querySetPrisoners = "INSERT INTO prisoners(player_name, x, y, z, world) VALUES (?, ?, ?, ?, ?)";
    public int setPrisoners(String playerName, double x, double y, double z, String world) {
        int numRowsInserted = 0;
        PreparedStatement ps = null;
        try {
            ps = this.connection.prepareStatement(querySetPrisoners);
            ps.setString(1, playerName);
            ps.setDouble(2, x);
            ps.setDouble(3, y);
            ps.setDouble(4, z);
            ps.setString(5, world);
            numRowsInserted = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close(ps);
        }
        return numRowsInserted;
    }

    public List<String> getAllCaves() {
        String query = "SELECT name FROM caves";
        List<String> names = new ArrayList<>();
        try {
            PreparedStatement st = this.connection.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return names;
    }
    public List<String> getAllPrisoners() {
        String query = "SELECT player_name FROM prisoners";
        List<String> prisoners = new ArrayList<>();
        try {
            PreparedStatement st = this.connection.prepareStatement(query);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                prisoners.add(rs.getString("player_name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return prisoners;
    }

    String queryGetCave = "SELECT * FROM caves WHERE name = ?";
    public String getCave(String name) {
        try {
            PreparedStatement st = this.connection.prepareStatement(queryGetCave);
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    String queryGetPrisoners = "SELECT * FROM prisoners WHERE player_name = ?";
    public String getPrisoners(String name) {
        try {
            PreparedStatement st = this.connection.prepareStatement(queryGetPrisoners);
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if(rs.next()) {
                return rs.getString("player_name");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int removePrisoners(String name) {
        int numRowsDeleted = 0;
        String query = "DELETE FROM prisoners WHERE player_name = ?";
        try {
            PreparedStatement ps = this.connection.prepareStatement(query);
            ps.setString(1, name);
            numRowsDeleted = ps.executeUpdate();
            return numRowsDeleted;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public double getCoordX(String caveName) throws SQLException {
        String queryGetCoord = "SELECT x FROM caves WHERE name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetCoord);
        st.setString(1, caveName);
        ResultSet rs = st.executeQuery();

        double x = 0;
        if (rs.next()) {
            x = rs.getDouble("x");
        }
        return x;
    }

    public double getPrisX(String name) throws SQLException {
        String queryGetPris = "SELECT x FROM prisoners WHERE player_name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetPris);
        st.setString(1, name);
        ResultSet rs = st.executeQuery();

        double x = 0;
        if (rs.next()) {
            x = rs.getDouble("x");
        }
        return x;
    }

    public String getWorldPreLoc(String name) throws SQLException {
        String queryGetWorld = "SELECT world FROM prisoners WHERE player_name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetWorld);
        st.setString(1, name);
        ResultSet rs = st.executeQuery();

        String world = null;
        if (rs.next()) {
            world = rs.getString("world");
        }
        return world;
    }

    public double getCoordY(String caveName) throws SQLException {
        String queryGetCoord = "SELECT y FROM caves WHERE name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetCoord);
        st.setString(1, caveName);
        ResultSet rs = st.executeQuery();

        double y = 0;
        if (rs.next()) {
            y = rs.getDouble("y");
        }
        return y;
    }

    public double getPrisY(String name) throws SQLException {
        String queryGetPris = "SELECT y FROM prisoners WHERE player_name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetPris);
        st.setString(1, name);
        ResultSet rs = st.executeQuery();

        double y = 0;
        if (rs.next()) {
            y = rs.getDouble("y");
        }
        return y;
    }

    public double getCoordZ(String caveName) throws SQLException {
        String queryGetCoord = "SELECT z FROM caves WHERE name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetCoord);
        st.setString(1, caveName);
        ResultSet rs = st.executeQuery();

        double z = 0;
        if (rs.next()) {
            z = rs.getDouble("z");
        }
        return z;
    }

    public double getPrisZ(String name) throws SQLException {
        String queryGetPris = "SELECT z FROM prisoners WHERE player_name = ?";
        PreparedStatement st = connection.prepareStatement(queryGetPris);
        st.setString(1, name);
        ResultSet rs = st.executeQuery();

        double z = 0;
        if (rs.next()) {
            z = rs.getDouble("z");
        }
        return z;
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
