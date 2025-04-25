import java.sql.*;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1522:xe", "system", "dbms123");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean registerAdmin(String adminName, String password) {
        try {
            String sql = "INSERT INTO admin (admin_name, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminName);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loginAdmin(String adminName, String password) {
        try {
            String sql = "SELECT admin_id FROM admin WHERE admin_name=? AND password=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminName);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("admin_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addAlumni(int adminId, String name, int gradYear) {
        try {
            int alumniId = -1;
            ResultSet rs = conn.prepareStatement("SELECT alumni_seq.NEXTVAL FROM dual").executeQuery();
            if (rs.next()) alumniId = rs.getInt(1);

            String sql1 = "INSERT INTO alumni (alumni_id, alumni_name, graduation_year) VALUES (?, ?, ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, alumniId);
            pstmt1.setString(2, name);
            pstmt1.setInt(3, gradYear);
            pstmt1.executeUpdate();

            String sql2 = "INSERT INTO manages (admin_id, alumni_id, date_added) VALUES (?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, adminId);
            pstmt2.setInt(2, alumniId);
            pstmt2.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            pstmt2.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean engageAlumni(int adminId, int alumniId, String type, String detail1, String detail2) {
        try {
            // Validate alumni belongs to this admin
            PreparedStatement validate = conn.prepareStatement("SELECT * FROM manages WHERE admin_id=? AND alumni_id=?");
            validate.setInt(1, adminId);
            validate.setInt(2, alumniId);
            ResultSet rs = validate.executeQuery();
            if (!rs.next()) return false;

            int id = -1;
            if (type.equals("Event")) {
                rs = conn.prepareStatement("SELECT event_seq.NEXTVAL FROM dual").executeQuery();
                if (rs.next()) id = rs.getInt(1);
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO events VALUES (?, ?, ?)");
                pstmt.setInt(1, id);
                pstmt.setString(2, detail1);
                pstmt.setDate(3, Date.valueOf(detail2));
                pstmt.executeUpdate();
            } else {
                rs = conn.prepareStatement("SELECT job_seq.NEXTVAL FROM dual").executeQuery();
                if (rs.next()) id = rs.getInt(1);
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO job_posting VALUES (?, ?, ?)");
                pstmt.setInt(1, id);
                pstmt.setString(2, detail1);
                pstmt.setString(3, detail2);
                pstmt.executeUpdate();
            }

            PreparedStatement engageStmt = conn.prepareStatement("INSERT INTO engages VALUES (?, ?, ?)");
            engageStmt.setInt(1, alumniId);
            engageStmt.setInt(2, id);
            engageStmt.setString(3, type);
            engageStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeEngagement(int alumniId, int id, String type) {
        try {
            String sql = "DELETE FROM engages WHERE alumni_id=? AND event_or_job_id=? AND engagement_type=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alumniId);
            stmt.setInt(2, id);
            stmt.setString(3, type);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getAlumniEngagements(int adminId) {
        StringBuilder result = new StringBuilder();
        try {
            String query = "SELECT a.alumni_id, a.alumni_name, e.engagement_type, " +
                    "ev.event_id, ev.event_name, ev.event_date, " +
                    "j.job_posting_id, j.job_title, j.job_description " +
                    "FROM alumni a " +
                    "JOIN manages m ON a.alumni_id = m.alumni_id " +
                    "LEFT JOIN engages e ON a.alumni_id = e.alumni_id " +
                    "LEFT JOIN events ev ON e.event_or_job_id = ev.event_id AND e.engagement_type = 'Event' " +
                    "LEFT JOIN job_posting j ON e.event_or_job_id = j.job_posting_id AND e.engagement_type = 'Job' " +
                    "WHERE m.admin_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int alumniId = rs.getInt("alumni_id");
                String name = rs.getString("alumni_name");
                String type = rs.getString("engagement_type");
                result.append("Alumni ID: ").append(alumniId).append("\nName: ").append(name).append("\n");
                if ("Event".equals(type)) {
                    result.append("Event ID: ").append(rs.getInt("event_id"))
                            .append(" | Name: ").append(rs.getString("event_name"))
                            .append(" | Date: ").append(rs.getDate("event_date")).append("\n");
                } else if ("Job".equals(type)) {
                    result.append("Job ID: ").append(rs.getInt("job_posting_id"))
                            .append(" | Title: ").append(rs.getString("job_title"))
                            .append(" | Description: ").append(rs.getString("job_description")).append("\n");
                }
                result.append("---------------\n");
            }
            if (result.length() == 0) return "No alumni added under this admin.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error retrieving engagements.";
        }
        return result.toString();
    }
}
