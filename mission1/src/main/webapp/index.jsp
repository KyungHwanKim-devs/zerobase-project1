<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>와이파이 정보 구하기</title>
</head>
<body>
<script>
    let latitude;
    let longitude
    const getMyLoc =(event) => {
        event.preventDefault();

        navigator.geolocation.getCurrentPosition((pos) =>{
            latitude = pos.coords.latitude;
            longitude = pos.coords.longitude;
            document.getElementById("lat").value = latitude;
            document.getElementById("lnt").value = longitude;
        });

    }

</script>
<h1>와이파이 정보 구하기</h1>
<br/>
    <div>
        <span><a href="/">홈</a> </span>
        |
        <span><a href="history.jsp">위치 히스토리 목록</a></span>
        |
        <span><a href="load-wifi.jsp">Open API 와이파이 정보 가져오기</a></span>
    </div>

<div style="height: 10px"></div>
<div>

    <form method="get" action="helloServlet">
        <label for="lat">LAT :</label><input id="lat" name="lat" type="text"> ,
        <label for="lnt">lnt :</label><input id="lnt" name="lnt" type="text"/>
        <button onclick="getMyLoc(event)">내 위치 가져오기</button>
        <button type="submit">근처 WIPI 정보 보기 </button>
    </form>

</div>
<table style="width: 100%">
    <thead>
        <tr>
            <th>거리(KM)</th>
            <th>관리번호</th>
            <th>자치구</th>
            <th>와이파이명</th>
            <th>도로명주소</th>
            <th>상세주소</th>
            <th>설치위치(층)</th>
            <th>설치유형</th>
            <th>설치기관</th>
            <th>서비스구분</th>
            <th>망종류</th>
            <th>설치년도</th>
            <th>실내외구분</th>
            <th>WIPI접속환경</th>
            <th>X좌표</th>
            <th>Y좌표</th>
            <th>작업일자</th>
        </tr>
    </thead>
    <tbody>
        <tr style="background-color: aqua; width: 100%">
            <th scope="col" colspan="17">위치 정보를 입력한 후에 조회해 주세요.</th>
        </tr>
    </tbody>
</table>
</body>
</html>