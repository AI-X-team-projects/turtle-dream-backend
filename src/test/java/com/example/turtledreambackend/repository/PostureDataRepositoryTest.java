package com.example.turtledreambackend.repository;

import com.example.turtledreambackend.data.entity.posture.PostureData;
import com.example.turtledreambackend.data.repository.posture.PostureDataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 작성자: 김동규
 * 작성일: 2025-03-13
 *
 * MongoDB 연동 테스트 (PostureData 저장 및 조회)
 */
@DataMongoTest  // MongoDB 관련 Bean만 로드하여 테스트
public class PostureDataRepositoryTest {

    @Autowired
    private PostureDataRepository postureDataRepository;

    @Test
    public void testSaveAndFindPostureData() {
        // 테스트 데이터 저장
        PostureData postureData = PostureData.builder()
                .userId("test_user")
                .isGoodPosture(false)
                .postureStatus("BAD")
                .feedback("나쁜 자세가 감지되었습니다.")
                .badPostureDuration(120)
                .totalSessionDuration(600)
                .recordedAt(LocalDateTime.now())
                .build();

        postureDataRepository.save(postureData);

        // 저장된 데이터 조회
        List<PostureData> result = postureDataRepository.findByUserId("test_user");

        // 검증 (Junit + AssertJ 사용)
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getPostureStatus()).isEqualTo("BAD");
        assertThat(result.get(0).getFeedback()).isEqualTo("나쁜 자세가 감지되었습니다.");
    }
}
