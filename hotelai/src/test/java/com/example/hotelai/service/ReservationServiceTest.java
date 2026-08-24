package com.example.hotelai.service;

import com.example.hotelai.entity.Reservation;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.StayRecordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // 测试结束后自动回滚，不污染数据库
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private StayRecordRepository stayRecordRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testReservationId;
    private final String testSessionId = "test-session-001";

    @BeforeEach
    void setUp() {
        // 插入一条测试预订记录（状态 BOOKED，入住日期为今天，离店日期为后天）
        String insertSql = "INSERT INTO RESERVATIONS (USER_ID, SESSION_ID, ROOM_TYPE_ID, CHECK_IN_DATE, CHECK_OUT_DATE, ROOM_COUNT, STATUS, CREATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql,
                testSessionId,           // USER_ID
                testSessionId,           // SESSION_ID
                1L,                      // ROOM_TYPE_ID（确保你的 ROOM_TYPES 表中有 id=1 的记录，如果没有请修改成实际存在的 id）
                LocalDate.now(),         // CHECK_IN_DATE
                LocalDate.now().plusDays(2), // CHECK_OUT_DATE
                1,                       // ROOM_COUNT
                "BOOKED",                // STATUS
                LocalDateTime.now()      // CREATED_AT
        );

        // 获取刚插入的预订 ID（Oracle 不支持 getGeneratedKeys 的简单写法，用查询方式）
        Long id = jdbcTemplate.queryForObject(
                "SELECT ID FROM RESERVATIONS WHERE SESSION_ID = ? AND STATUS = 'BOOKED' AND ROWNUM = 1",
                Long.class, testSessionId);
        testReservationId = id;
        System.out.println("测试预订 ID: " + testReservationId);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据（由 @Transactional 自动回滚，其实不需要手动删，但为了安心保留）
        if (testReservationId != null) {
            stayRecordRepository.findByReservationId(testReservationId)
                    .ifPresent(record -> stayRecordRepository.delete(record));
            reservationRepository.deleteById(testReservationId);
        }
    }

    @Test
    void testCheckIn_Success() {
        // 调用入住方法
        Map<String, String> result = reservationService.checkIn(testReservationId, testSessionId);

        // 验证返回结果
        assertThat(result.get("code")).isEqualTo("200");
        assertThat(result.get("message")).isEqualTo("入住办理成功");

        // 验证数据库中的状态已变更为 CHECKED_IN
        Reservation updated = reservationRepository.findById(testReservationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("CHECKED_IN");

        // 验证 stay_records 表中增加了记录
        assertThat(stayRecordRepository.findByReservationId(testReservationId)).isPresent();
    }

    @Test
    void testCheckIn_Fail_WrongSession() {
        // 使用不同的 sessionId，应该返回无权操作
        Map<String, String> result = reservationService.checkIn(testReservationId, "wrong-session");
        assertThat(result.get("code")).isEqualTo("500");
        assertThat(result.get("message")).isEqualTo("无权操作此订单");
    }

    @Test
    void testCheckIn_Fail_AlreadyCheckedIn() {
        // 先入住一次
        reservationService.checkIn(testReservationId, testSessionId);
        // 再次入住应该失败
        Map<String, String> result = reservationService.checkIn(testReservationId, testSessionId);
        assertThat(result.get("code")).isEqualTo("500");
        assertThat(result.get("message")).isEqualTo("当前订单状态不可办理入住");
    }

    @Test
    void testCheckOut_Success() throws Exception {
        // 先入住
        reservationService.checkIn(testReservationId, testSessionId);
        // 然后离店
        Map<String, String> result = reservationService.checkOut(testReservationId, testSessionId);
        assertThat(result.get("code")).isEqualTo("200");
        assertThat(result.get("message")).isEqualTo("离店办理成功");

        // 验证状态变为 COMPLETED
        Reservation updated = reservationRepository.findById(testReservationId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("COMPLETED");

        // 验证 stay_records 中记录了离店时间
        var stayRecord = stayRecordRepository.findByReservationId(testReservationId).orElseThrow();
        assertThat(stayRecord.getActualCheckOut()).isNotNull();
    }

    @Test
    void testCheckOut_Fail_NotCheckedIn() {
        // 未入住直接离店，应该失败
        Map<String, String> result = reservationService.checkOut(testReservationId, testSessionId);
        assertThat(result.get("code")).isEqualTo("500");
        assertThat(result.get("message")).isEqualTo("当前订单未办理入住，无法离店");
    }
}