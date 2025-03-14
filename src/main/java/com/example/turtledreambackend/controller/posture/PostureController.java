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
//    @GetMapping("/daily")
//    public ResponseEntity<List<PostureData>> getDailyPosture(
//            @RequestParam String userId,
//            @RequestParam(required = false) String date) {
//        if (date == null || date.isEmpty()) {
//            date = LocalDate.now().toString();
//        }
//        LocalDate localDate = LocalDate.parse(date);
//        List<PostureData> data = postureService.getDailyPostureData(userId, localDate);
//        return ResponseEntity.ok(data);
//    }

    @GetMapping("/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyPosture(
            @RequestParam String userId,
            @RequestParam String date) {

        LocalDate localDate = LocalDate.parse(date);
        List<PostureData> dataList = postureService.getDailyPostureData(userId, localDate);

        // 🔹 데이터 변환 시 명확한 타입 지정
        List<Map<String, Object>> transformedData = dataList.stream().map(data -> {
            Map<String, Object> map = new HashMap<>();
//            map.put("recordedAt", data.getRecordedAt().toLocalTime().toString().substring(0, 5)); // HH:MM
//            map.put("badPostureCount", data.isGoodPosture() ? 0 : 1);  // 나쁜 자세 횟수 변환
            map.put("recordedAt", data.getRecordedAt().toString()); // ISO-8601 형식 유지
            map.put("badPostureDuration", data.getBadPostureDuration());
            return map;
        }).toList();

        return ResponseEntity.ok(transformedData);
    }

    /**
     * 특정 월의 자세 데이터 조회 (월별 분석)
     *
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 자세 데이터 목록
     */
//    @GetMapping("/monthly")
//    public ResponseEntity<List<PostureData>> getMonthlyPosture(
//            @RequestParam String userId,
//            @RequestParam int year,
//            @RequestParam int month) {
//        List<PostureData> data = postureService.getMonthlyPostureData(userId, year, month);
//        return ResponseEntity.ok(data);
//    }

    @GetMapping("/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyPosture(
            @RequestParam String userId,
            @RequestParam int year,
            @RequestParam int month) {

        List<PostureSummary> dataList = postureService.getMonthlyPostureData(userId, year, month);

        // 데이터 변환 시 명확한 타입 지정
        List<Map<String, Object>> transformedData = dataList.stream().map(summary -> {
            Map<String, Object> map = new HashMap<>();
//            map.put("month", summary.getSummaryDate().substring(5, 7) + "월"); // "MM월" 형식
            map.put("summaryDate", summary.getSummaryDate());
            map.put("goodPostureCount", summary.getTotalGoodPosture());
            map.put("badPostureCount", summary.getTotalBadPosture());
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
            System.out.println("AI 분석 데이터 저장 요청 받음: " + postureData);
            postureData.setRecordedAt(LocalDateTime.now());
            System.out.println("자세 저장 날짜 : " + postureData.getRecordedAt());
            System.out.println("사용자 아이디 : " + postureData.getUserId());
            postureService.savePosture(postureData);
            System.out.println("자세 데이터 저장 성공");
            return ResponseEntity.ok("자세 데이터 저장 완료");
        } catch (Exception e) {
            System.err.println("데이터 저장 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body("데이터 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 테스트 데이터 저장용
     * */
    @PostMapping("/mock-daily")
    public ResponseEntity<String> generateMockDailyData(@RequestParam String userId, @RequestParam String date) {
        try {
            List<PostureData> mockDataList = new ArrayList<>();
            LocalDate localDate = LocalDate.parse(date);

            for (int hour = 9; hour < 18; hour++) { // 09:00 ~ 18:00
                for (int i = 0; i < 5; i++) { // 각 시간당 5개 생성
                    int minute = random.nextInt(60); // 랜덤 분(0~59)
                    LocalDateTime recordedAt = localDate.atTime(hour, minute);

                    boolean isGood = random.nextDouble() > 0.5; // 50% 확률
                    int badPostureDuration = isGood ? 0 : random.nextInt(10); // 0~10초 랜덤

                    PostureData mockData = PostureData.builder()
                            .userId(userId)
                            .isGoodPosture(isGood)
                            .postureStatus(isGood ? "GOOD" : "BAD")
                            .feedback(isGood ? "바른 자세입니다!" : "턱을 들어 목을 펴주세요.")
                            .recordedAt(recordedAt != null ? recordedAt : LocalDateTime.now()) // null 방지
                            .badPostureDuration(badPostureDuration)
                            .totalSessionDuration(3600)
                            .build();

                    mockDataList.add(mockData);
                }
            }

            postureRepository.saveAll(mockDataList);
            return ResponseEntity.ok("09:00~18:00 목업 데이터 저장 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("목업 데이터 저장 실패: " + e.getMessage());
        }
    }


}
