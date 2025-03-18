package com.example.turtledreambackend.controller.posture;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.entity.posture.PostureSummary;
import com.example.turtledreambackend.data.repository.posture.PostureRepository;
import com.example.turtledreambackend.service.posture.PostureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * 작성자 : 김동규
 * 작성일 : 2025-03-13
 *
 * 사용자 자세 데이터를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/posture")
@RequiredArgsConstructor
public class PostureController {

    private final PostureService postureService;

    private final PostureRepository postureRepository;
    private final Random random = new Random();

    /**
     * 특정 날짜의 자세 데이터 조회 (일일 분석)
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 날짜의 자세 데이터 목록
     */
    @GetMapping("/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyPosture(
            @RequestParam String userId,
            @RequestParam String date) {

//        System.out.println("요청 받음: userId=" + userId + ", date=" + date);

        LocalDate localDate = LocalDate.parse(date);
        List<PostureData> dataList = postureService.getDailyPostureData(userId, localDate);


//        System.out.println("조회된 데이터 개수: " + dataList.size());

        // 데이터 변환 시 명확한 타입 지정
        List<Map<String, Object>> transformedData = dataList.stream().map(data -> {
            Map<String, Object> map = new HashMap<>();
//            map.put("recordedAt", data.getRecordedAt().toLocalTime().toString().substring(0, 5)); // HH:MM
//            map.put("badPostureCount", data.isGoodPosture() ? 0 : 1);  // 나쁜 자세 횟수 변환
            map.put("recordedAt", data.getRecordedAt().toString()); // ISO-8601 형식 유지
            map.put("badPostureDuration", data.getBadPostureDuration());
            return map;
        }).toList();

//        System.out.println("변환된 데이터 개수: " + transformedData.size());

        return ResponseEntity.ok(transformedData);
    }

    /**
     * 특정 월의 자세 데이터 조회 (월별 분석)
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param startDate 조회할 시작 일자
     * @param endDate 조회할 종료 일자
     * @return 해당 월의 자세 데이터 목록
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyPosture(
            @RequestParam String userId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<PostureData> dataList = postureService.getPostureDataByDateRange(userId, start, end);

        Map<String, Map<String, Integer>> groupedData = new LinkedHashMap<>();

        for (PostureData data : dataList) {
            LocalDate localDate = data.getRecordedAt().toLocalDate(); // 정확한 로컬 날짜 변환

            String dateKey = localDate.toString(); // YYYY-MM-DD 형식 유지

            groupedData.putIfAbsent(dateKey, new HashMap<>());
            groupedData.get(dateKey).put("goodPostureCount", groupedData.get(dateKey).getOrDefault("goodPostureCount", 0));
            groupedData.get(dateKey).put("badPostureCount", groupedData.get(dateKey).getOrDefault("badPostureCount", 0));

            if (data.isGoodPosture()) {
                groupedData.get(dateKey).put("goodPostureCount", groupedData.get(dateKey).get("goodPostureCount") + 1);
            } else {
                groupedData.get(dateKey).put("badPostureCount", groupedData.get(dateKey).get("badPostureCount") + data.getBadPostureDuration());
            }
        }

        List<Map<String, Object>> transformedData = groupedData.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("summaryDate", entry.getKey()); // YYYY-MM-DD 그대로 전달
            map.put("goodPostureCount", entry.getValue().get("goodPostureCount"));
            map.put("badPostureCount", entry.getValue().get("badPostureCount"));
            return map;
        }).toList();

        return ResponseEntity.ok(transformedData);
    }



    /**
     * AI 분석 데이터를 저장하는 엔드포인트
     *
     * @param postureData 사용자의 자세 데이터 (JSON 형식으로 요청 바디에서 받음)
     * @return 저장 성공 시 "자세 데이터 저장 완료" 메시지 반환, 실패 시 오류 메시지 반환
     */
    @PostMapping("/save")
    public ResponseEntity<String> savePostureData(@RequestBody PostureData postureData) {
        try {
//            System.out.println("AI 분석 데이터 저장 요청 받음: " + postureData);
            postureData.setRecordedAt(LocalDateTime.now());
//            System.out.println("자세 저장 날짜 : " + postureData.getRecordedAt());
//            System.out.println("사용자 아이디 : " + postureData.getUserId());
            postureService.savePosture(postureData);
//            System.out.println("자세 데이터 저장 성공");
            return ResponseEntity.ok("자세 데이터 저장 완료");
        } catch (Exception e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body("데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 상위 3개 나쁜 자세 시간대 조회 API
     */
    @GetMapping("/badPostureHours")
    public ResponseEntity<List<Map<String, Object>>> getTopBadPostureHours(
            @RequestParam String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        List<Map<String, Object>> result = postureService.getTopBadPostureHours(userId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * 테스트 데이터 저장용
     * */
    @PostMapping("/mock-daily")
    public ResponseEntity<String> generateMockDailyData(@RequestParam("userId") String userId, @RequestParam("date") String date) {
        try {
            List<PostureData> mockDataList = new ArrayList<>();
            LocalDate localDate = LocalDate.parse(date);
            Random random = new Random();

            Set<LocalDateTime> uniqueTimes = new HashSet<>(); // 중복 방지

            while (uniqueTimes.size() < 12) { // 최대 12개의 데이터만 생성
                int hour = random.nextInt(10) + 9; // 09:00 ~ 18:00 랜덤 시간 (9~18)
//                int hour = random.nextInt(13) + 9; // 09:00 ~ 21:00 사이의 랜덤 시간 (9~21)
                int minute = random.nextInt(60); // 00~59 랜덤 분
                LocalDateTime recordedAt = localDate.atTime(hour, minute);

                if (uniqueTimes.add(recordedAt)) { // 중복되지 않으면 추가
                    boolean isGood = random.nextDouble() > 0.3; // 70% 확률로 나쁜 자세
                    int badPostureDuration = isGood ? 0 : random.nextInt(15) + 5; // 5~20초 랜덤

                    PostureData mockData = PostureData.builder()
                            .userId(userId)
                            .isGoodPosture(isGood)
                            .postureStatus(isGood ? "GOOD" : "BAD")
                            .feedback(isGood ? "바른 자세입니다!" : "턱을 들어 목을 펴주세요.")
                            .recordedAt(recordedAt)
                            .badPostureDuration(badPostureDuration)
                            .totalSessionDuration(3600)
                            .build();

                    mockDataList.add(mockData);
                }
            }

            postureRepository.saveAll(mockDataList);
            return ResponseEntity.ok("09:00~18:00 사이 랜덤 12개 목업 데이터 저장 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("목업 데이터 저장 실패: " + e.getMessage());
        }

    }

    @PostMapping("/mock-daily-range")
    public ResponseEntity<String> generateMockDailyDataForRange(@RequestParam("userId") String userId) {
        try {
            List<PostureData> allMockData = new ArrayList<>();
            Random random = new Random();

            LocalDate startDate = LocalDate.of(2025, 3, 10); // 2025-03-10
            LocalDate endDate = LocalDate.of(2025, 3, 16);   // 2025-03-17

            for (LocalDate localDate = startDate; !localDate.isAfter(endDate); localDate = localDate.plusDays(1)) {
                Set<LocalDateTime> uniqueTimes = new HashSet<>(); // 중복 방지

                while (uniqueTimes.size() < 12) { // 하루에 최대 12개의 데이터
                    int hour = random.nextInt(10) + 9; // 09:00 ~ 18:00 랜덤 시간 (9~18)
//                    int hour = random.nextInt(13) + 9; // 09:00 ~ 21:00 사이의 랜덤 시간 (9~21)
                    int minute = random.nextInt(60); // 00~59 랜덤 분
                    LocalDateTime recordedAt = localDate.atTime(hour, minute);

                    if (uniqueTimes.add(recordedAt)) { // 중복되지 않으면 추가
                        boolean isGood = random.nextDouble() > 0.3; // 70% 확률로 나쁜 자세
                        int badPostureDuration = isGood ? 0 : random.nextInt(15) + 5; // 5~20초 랜덤

                        PostureData mockData = PostureData.builder()
                                .userId(userId)
                                .isGoodPosture(isGood)
                                .postureStatus(isGood ? "GOOD" : "BAD")
                                .feedback(isGood ? "바른 자세입니다!" : "턱을 들어 목을 펴주세요.")
                                .recordedAt(recordedAt)
                                .badPostureDuration(badPostureDuration)
                                .totalSessionDuration(3600)
                                .build();

                        allMockData.add(mockData);
                    }
                }
            }

            postureRepository.saveAll(allMockData);
            return ResponseEntity.ok("2025-03-10 ~ 2025-03-17 랜덤 12개씩 목업 데이터 저장 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("목업 데이터 저장 실패: " + e.getMessage());
        }
    }

}
