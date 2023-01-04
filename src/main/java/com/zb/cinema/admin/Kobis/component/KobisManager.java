package com.zb.cinema.admin.Kobis.component;

import com.zb.cinema.admin.entity.MovieCode;
import com.zb.cinema.admin.entity.MovieInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class KobisManager {

    private final String BASE_URL = "http://kobis.or.kr/kobisopenapi/webservice/rest";
    private final String serviceKey = "48656dce18398a0341dd2159167d8440";

    private String makeBoxOfficeResultUrl(String date) throws UnsupportedEncodingException {
        return BASE_URL +
            "/boxoffice/searchDailyBoxOfficeList.json?key=" +
            serviceKey +
            "&targetDt=" +
            date;
    }

    public List<MovieCode> fetchBoxOfficeResult(String date)
        throws ParseException, UnsupportedEncodingException, org.json.simple.parser.ParseException {
        RestTemplate restTemplate = new RestTemplate();
        String jsonString = restTemplate.getForObject(makeBoxOfficeResultUrl(date), String.class);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);
        JSONObject jsonResponse = (JSONObject) jsonObject.get("boxOfficeResult");
        JSONArray jsonItemList = (JSONArray) jsonResponse.get("dailyBoxOfficeList");

        List<MovieCode> result = new ArrayList<>();

        for (Object o : jsonItemList) {
            JSONObject item = (JSONObject) o;
            result.add(makeMovieCodeDto(item));
        }
        return result;
    }

    private MovieCode makeMovieCodeDto(JSONObject item) {
        return MovieCode.builder()
            .code(Long.parseLong((String) item.get("movieCd")))
            .title((String) item.get("movieNm"))
            .build();
    }

    private String makeMovieInfoResultUrl(long movieCode) throws UnsupportedEncodingException {
        return BASE_URL +
            "/movie/searchMovieInfo.json?key=" +
            serviceKey +
            "&movieCd=" +
            movieCode;
    }

    private String arrangeStr(String str) {
        if (str == "") {
            return "";
        }
        return str.substring(0, str.length() - 2);
    }

    private MovieInfo makeMovieInfoDto(JSONObject item) throws ParseException {
        String actors = "";
        String director = "";
        String genre = "";
        String nation = "";

        JSONArray nationsList = (JSONArray) item.get("nations");
        for (Object o : nationsList) {
            JSONObject nationName = (JSONObject) o;
            nation += (String) nationName.get("nationNm");
            nation += ", ";
        }

        JSONArray actorList = (JSONArray) item.get("actors");
        for (Object o : actorList) {
            JSONObject actorName = (JSONObject) o;
            actors += (String) actorName.get("peopleNm");
            actors += ", ";
        }

        JSONArray directorList = (JSONArray) item.get("directors");
        for (Object o : directorList) {
            JSONObject directorName = (JSONObject) o;
            director += (String) directorName.get("peopleNm");
            director += ", ";
        }

        JSONArray genreList = (JSONArray) item.get("genres");
        for (Object o : genreList) {
            JSONObject genreName = (JSONObject) o;
            genre += (String) genreName.get("genreNm");
            genre += ", ";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        LocalDateTime openDt = format.parse((String) item.get("openDt"))
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

        MovieInfo build = MovieInfo.builder()
            .code(Long.parseLong((String) item.get("movieCd")))
            .title((String) item.get("movieNm"))
            .actors(arrangeStr(actors))
            .directors(arrangeStr(director))
            .genre(arrangeStr(genre))
            .nation(arrangeStr(nation))
            .runTime(Long.parseLong((String) item.get("showTm")))
            .openDt(openDt)
            .build();
        return build;
    }

    public MovieInfo fetchMovieInfoResult(Long movieCode)
        throws ParseException, UnsupportedEncodingException, org.json.simple.parser.ParseException {
        RestTemplate restTemplate = new RestTemplate();
        String jsonString = restTemplate.getForObject(makeMovieInfoResultUrl(movieCode),
            String.class);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonString);
        JSONObject jsonResponse = (JSONObject) jsonObject.get("movieInfoResult");
        JSONObject jsonItemList = (JSONObject) jsonResponse.get("movieInfo");

        MovieInfo result = makeMovieInfoDto(jsonItemList);

        return result;
    }

}
