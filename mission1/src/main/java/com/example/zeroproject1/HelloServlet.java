package com.example.zeroproject1;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.DTO.WIFIDATA;
import com.common.COMMON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/helloServlet")
public class HelloServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("request.getParameter()" + request.getParameter("lat"));
        System.out.println("request.getParameter()" + request.getParameter("lnt"));

        Connection conn = null;
        PreparedStatement pstmt = null;
        List<WIFIDATA> list = new ArrayList<>();

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mariadb://localhost:3306/test";
            conn = DriverManager.getConnection(url, "root", "1122");

            String sql = "Select * FROM WIFIINFO";

            pstmt = conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                WIFIDATA wifidata = new WIFIDATA();
                wifidata.setX_SWIFI_MGR_NO(rs.getString("X_SWIFI_MGR_NO"));
                wifidata.setX_SWIFI_WRDOFC(rs.getString("X_SWIFI_WRDOFC"));
                wifidata.setX_SWIFI_MAIN_NM(rs.getString("X_SWIFI_MAIN_NM"));
                wifidata.setX_SWIFI_ADRES1(rs.getString("X_SWIFI_ADRES1"));
                wifidata.setX_SWIFI_ADRES2(rs.getString("X_SWIFI_ADRES2"));
                wifidata.setX_SWIFI_INSTL_FLOOR(rs.getString("X_SWIFI_INSTL_FLOOR"));
                wifidata.setX_SWIFI_INSTL_TY(rs.getString("X_SWIFI_INSTL_TY"));
                wifidata.setX_SWIFI_INSTL_MBY(rs.getString("X_SWIFI_INSTL_MBY"));
                wifidata.setX_SWIFI_SVC_SE(rs.getString("X_SWIFI_SVC_SE"));
                wifidata.setX_SWIFI_CMCWR(rs.getString("X_SWIFI_CMCWR"));
                wifidata.setX_SWIFI_CNSTC_YEAR(rs.getString("X_SWIFI_CNSTC_YEAR"));
                wifidata.setX_SWIFI_INOUT_DOOR(rs.getString("X_SWIFI_INOUT_DOOR"));
                wifidata.setX_SWIFI_REMARS3(rs.getString("X_SWIFI_REMARS3"));
                wifidata.setWORK_DTTM(rs.getString("WORK_DTTM"));



                wifidata.setLAT(Double.parseDouble(rs.getString("LAT").replaceAll("\"", "")));
                wifidata.setLNT(Double.parseDouble(rs.getString("LNT").replaceAll("\"", "")));
                wifidata.setDistance(
                    COMMON.getDistance(Double.parseDouble(rs.getString("LAT").replaceAll("\"", "")),
                                        Double.parseDouble(rs.getString("LNT").replaceAll("\"", "")),
                                        Double.parseDouble(request.getParameter("lat")),
                                        Double.parseDouble(request.getParameter("lnt")))
                    );
                list.add(wifidata);
            }
            Collections.sort(list,(Comparator.comparingDouble(WIFIDATA::getDistance)));

        } catch (ClassNotFoundException e) {
            System.out.println("In servlet mariaDB Driver??? ????????? ????????????.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("In servlet Database ????????? ????????? ?????? ????????????.");
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


        Connection conn2 = null;
        PreparedStatement pstmt2 = null;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mariadb://localhost:3306/test";
            conn2 = DriverManager.getConnection(url, "root", "1122");

            String sql2 = "INSERT INTO MYLOC(" +
                    "LAT" +
                    ", LNT" +
                    ", WORK_DTTM" +
                    " ) VALUES (?, ?, ?)";

            pstmt2 = conn2.prepareStatement(sql2);
            ;

            pstmt2.setString(1, request.getParameter("lat"));
            pstmt2.setString(2, request.getParameter("lnt"));
            pstmt2.setString(3, LocalDateTime.now().toString());
//            pstmt2.setString(3, "asd");


//            int result =
            pstmt2.executeUpdate();
//            if (result == 0) {
//                System.out.println("??????????????? ????????? ????????????.");
//            } else {
//                System.out.println("????????? Insert ??????!");
//            }

        } catch (SQLException | ClassNotFoundException e) {
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

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>???????????? ?????? ?????????</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<script>\n" +
                "    let latitude;\n" +
                "    let longitude\n" +
                "    const getMyLoc =(event) => {\n" +
                "        event.preventDefault();\n" +
                "\n" +
                "        navigator.geolocation.getCurrentPosition((pos) =>{\n" +
                "            latitude = pos.coords.latitude;\n" +
                "            longitude = pos.coords.longitude;\n" +
                "            document.getElementById(\"lat\").value = latitude;\n" +
                "            document.getElementById(\"lnt\").value = longitude;\n" +
                "        });\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "</script>\n" +
                "<h1>???????????? ?????? ?????????</h1>\n" +
                "<br/>\n" +
                "    <div>\n" +
                "        <span><a href=\"/\">???</a> </span>\n" +
                "        |\n" +
                "        <span><a href=\"history.jsp\">?????? ???????????? ??????</a></span>\n" +
                "        |\n" +
                "        <span><a href=\"load-wifi.jsp\">Open API ???????????? ?????? ????????????</a></span>\n" +
                "    </div>\n" +
                "\n" +
                "<div style=\"height: 10px\"></div>\n" +
                "<div>\n" +
                "\n" +
                "    <form method=\"get\" action=\"helloServlet\">\n" +
                "        <label for=\"lat\">LAT :</label><input id=\"lat\" name=\"lat\" type=\"text\"> ,\n" +
                "        <label for=\"lnt\">LAT :</label><input id=\"lnt\" name=\"lnt\" type=\"text\"/>\n" +
                "        <button onclick=\"getMyLoc(event)\">??? ?????? ????????????</button>\n" +
                "        <button type=\"submit\">?????? WIPI ?????? ?????? </button>\n" +
                "    </form>\n" +
                "\n" +
                "</div>\n" +
                "<table style=\"width: 100%\">\n" +
                "    <thead>\n" +
                "        <tr>\n" +
                "            <th>??????\n(KM)</th>\n" +
                "            <th>????????????</th>\n" +
                "            <th>?????????</th>\n" +
                "            <th>???????????????</th>\n" +
                "            <th>???????????????</th>\n" +
                "            <th>????????????</th>\n" +
                "            <th>????????????\n(???)</th>\n" +
                "            <th>????????????</th>\n" +
                "            <th>????????????</th>\n" +
                "            <th>???????????????</th>\n" +
                "            <th>?????????</th>\n" +
                "            <th>????????????</th>\n" +
                "            <th>???????????????</th>\n" +
                "            <th>WIPI????????????</th>\n" +
                "            <th>X??????</th>\n" +
                "            <th>Y??????</th>\n" +
                "            <th>????????????</th>\n" +
                "        </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" );

        for (int i = 0; i < 20; i++) {
            out.println(
                    "<tr>"+
                    "<th>"+ Math.round(list.get(i).getDistance() * 10000) / 10000.0 +"</th>\n" +
                    "<th>"+ list.get(i).getX_SWIFI_MGR_NO() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_WRDOFC() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_MAIN_NM() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_ADRES1() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_ADRES2() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_INSTL_FLOOR() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_INSTL_TY() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_INSTL_MBY() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_SVC_SE() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_CMCWR() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_CNSTC_YEAR() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_INOUT_DOOR() +"</th>\n"+
                    "<th>"+ list.get(i).getX_SWIFI_REMARS3() +"</th>\n"+
                    "<th>"+ list.get(i).getLAT() +"</th>\n"+
                    "<th>"+ list.get(i).getLNT() +"</th>\n"+
                    "<th>"+ list.get(i).getWORK_DTTM() +"</th>\n"+
                    "</tr>");
        }

        out.println("</tbody>\n" +
                    "</table>\n" +
                    "</body>\n" +
                    "</html>");

    }


    public void destroy() {
    }
}