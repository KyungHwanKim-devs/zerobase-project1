<%@ page import="com.DTO.WIFIDATA" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.common.COMMON" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Comparator" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.DTO.MYLOC" %><%--
  Created by IntelliJ IDEA.
  User: kyunghwankim
  Date: 2022/12/27
  Time: 11:13 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>와이파이 정보 구하기</title>
</head>
<body>
    <h1>위치 히스토리 목록</h1>
    <div>
        <span><a href="/">홈</a> </span>
        |
        <span><a href="history.jsp">위치 히스토리 목록</a></span>
        |
        <span><a href="load-wifi.jsp">Open API 와이파이 정보 가져오기</a></span>
    </div>

    <table style="width: 100%">
        <thead>
        <tr>
            <th>ID</th>
            <th>X좌표</th>
            <th>Y좌표</th>
            <th>조회일자</th>
            <th>비고</th>
        </tr>
        </thead>
        <tbody>

        <%
            Connection conn = null;
            PreparedStatement pstmt = null;
            List<MYLOC> list = new ArrayList<>();

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://localhost:3306/test";
                conn = DriverManager.getConnection(url, "root", "1122");

                String sql = "Select * FROM MYLOC";

                pstmt = conn.prepareStatement(sql);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    MYLOC myloc = new MYLOC();
                    myloc.setId(rs.getString("id"));
                    myloc.setLAT(Double.parseDouble(rs.getString("LAT").replaceAll("\"", "")));
                    myloc.setLNT(Double.parseDouble(rs.getString("LNT").replaceAll("\"", "")));
                    myloc.setWORK_DTTM(rs.getString("WORK_DTTM"));

                    list.add(myloc);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("In servlet mariaDB Driver를 찾을수 없습니다.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("In servlet Database 연결중 에러가 발생 했습니다.");
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        %>

        <% for (int i = 0; i < list.size(); i++) { %>
        <tr>
            <td>
                <%= list.get(i).getId() %>
            </td>

            <td>
                <%= list.get(i).getLAT() %>
            </td>

            <td>
                <%= list.get(i).getLNT() %>
            </td>

            <td>
                <%= list.get(i).getWORK_DTTM() %>
            </td>

            <td>
                <form method="get">
                    <button name="id" value="<%= list.get(i).getId() %>" type="submit">삭제</button>
                </form>
                <%
                    if (request.getParameter("id") != null) {
                        Connection conn3 = null;
                        PreparedStatement pstmt3 = null;

                        try {
                            Class.forName("org.mariadb.jdbc.Driver");
                            String url = "jdbc:mariadb://localhost:3306/test";
                            conn3 = DriverManager.getConnection(url, "root", "1122");

                            String sql3 = "DELETE FROM MYLOC "
                                    + "WHERE id = ?";

                            pstmt3 = conn3.prepareStatement(sql3);

                            pstmt3.setString(1, list.get(i).getId());

                            int res = pstmt3.executeUpdate();

                            if (res > 0) {
                                System.out.println("삭제 성공");
                            }


                        } catch (ClassNotFoundException e) {
                            System.out.println("In servlet mariaDB Driver를 찾을수 없습니다.");
                            e.printStackTrace();
                        } catch (SQLException e) {
                            System.out.println("In servlet Database 연결중 에러가 발생 했습니다.");
                            e.printStackTrace();
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                %>
            </td>
        </tr>
        <% } %>

        </tbody>
    </table>
</body>
</html>
