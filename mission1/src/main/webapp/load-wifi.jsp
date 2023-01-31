<%--
  Created by IntelliJ IDEA.
  User: kyunghwankim
  Date: 2022/12/27
  Time: 11:07 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.*" %>
<%@ page import="com.squareup.okhttp.*" %>
<%@ page import="java.sql.Connection"%>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>와이파이 정보 구하기</title>
</head>
<body>

<%
    for (int j = 1; j <= 20; j++) {
        int b = 1000 * j;
        int a = b - 999;

        try {
            // OkHttp 클라이언트 객체 생성
            OkHttpClient client = new OkHttpClient();

            // GET 요청 객체 생성
            Request req = new Request.Builder()
                    .url("http://openapi.seoul.go.kr:8088/4b784e4f5a7275643133367646724273/json/TbPublicWifiInfo/" + a + "/" + b)
                    .get()
                    .build();

            // OkHttp 클라이언트로 GET 요청 객체 전송
            Response res = client.newCall(req).execute();

            if (res.isSuccessful()) {
                // 응답 받아서 처리
                ResponseBody body = res.body();
                if (body != null) {
                    String str = body.string();
                    String splitStr = str.substring(104, str.length() - 2);

                    JsonParser jsonParser = new JsonParser();
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(splitStr);
                    // 반복문 시작
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                        Connection conn = null;
                        PreparedStatement pstmt = null;

                        try {
                            Class.forName("org.mariadb.jdbc.Driver");
                            String url = "jdbc:mariadb://localhost:3306/test";
                            conn = DriverManager.getConnection(url, "root", "1122");
//                        System.out.println("DB 연결 성공");

                            String sql = " INSERT INTO WIFIINFO(" +
                                    "X_SWIFI_MGR_NO" +
                                    ", X_SWIFI_WRDOFC" +
                                    ", X_SWIFI_MAIN_NM" +
                                    ", X_SWIFI_ADRES1" +
                                    ", X_SWIFI_ADRES2" +
                                    ", X_SWIFI_INSTL_FLOOR" +
                                    ", X_SWIFI_INSTL_TY" +
                                    ", X_SWIFI_INSTL_MBY" +
                                    ",X_SWIFI_SVC_SE" +
                                    ",X_SWIFI_CMCWR" +
                                    ",X_SWIFI_CNSTC_YEAR" +
                                    ",X_SWIFI_INOUT_DOOR" +
                                    ",X_SWIFI_REMARS3" +
                                    ",LAT" +
                                    ",LNT" +
                                    ",WORK_DTTM" +
                                    ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                            pstmt = conn.prepareStatement(sql);

                            pstmt.setString(1, jsonObject.get("X_SWIFI_MGR_NO").toString());
                            pstmt.setString(2, jsonObject.get("X_SWIFI_WRDOFC").toString());
                            pstmt.setString(3, jsonObject.get("X_SWIFI_MAIN_NM").toString());
                            pstmt.setString(4, jsonObject.get("X_SWIFI_ADRES1").toString());
                            pstmt.setString(5, jsonObject.get("X_SWIFI_ADRES2").toString());
                            pstmt.setString(6, jsonObject.get("X_SWIFI_INSTL_FLOOR").toString());
                            pstmt.setString(7, jsonObject.get("X_SWIFI_INSTL_TY").toString());
                            pstmt.setString(8, jsonObject.get("X_SWIFI_INSTL_MBY").toString());
                            pstmt.setString(9, jsonObject.get("X_SWIFI_SVC_SE").toString());
                            pstmt.setString(10, jsonObject.get("X_SWIFI_CMCWR").toString());
                            pstmt.setString(11, jsonObject.get("X_SWIFI_CNSTC_YEAR").toString());
                            pstmt.setString(12, jsonObject.get("X_SWIFI_INOUT_DOOR").toString());
                            pstmt.setString(13, jsonObject.get("X_SWIFI_REMARS3").toString());
                            pstmt.setString(14, jsonObject.get("LAT").toString());
                            pstmt.setString(15, jsonObject.get("LNT").toString());
                            pstmt.setString(16, jsonObject.get("WORK_DTTM").toString());


                            // SQL실행
//                        int r =
                            pstmt.executeUpdate();

//                        System.out.println("변경된 row : " + r);



                        } catch (ClassNotFoundException e) {
                            System.out.println("mariaDB Driver를 찾을수 없습니다.");
                        } catch (SQLException e) {
                            System.out.println("Database 연결중 에러가 발생 했습니다.");
                        }finally {
                            try {
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    }




                }

            } else {
                System.err.println("Error Occurred");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


%>

<h1>19074 개의 WIPI 정보를 정상적으로 저장하였습니다.</h1>
<p><a href="/">홈 으로 가기</a></p>
</body>
</html>
