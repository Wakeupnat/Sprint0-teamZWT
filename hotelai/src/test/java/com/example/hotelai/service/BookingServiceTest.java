package com.example.hotelai.service;

import com.example.hotelai.entity.Reservation;
import com.example.hotelai.entity.RoomType;
import com.example.hotelai.repository.ReservationRepository;
import com.example.hotelai.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private BookingService bookingService;

    private RoomType testRoomType;
    private LocalDate validCheckIn;
    private LocalDate validCheckOut;
    private final String TEST_USER_ID = "test-user-123";
    private final String TEST_SESSION_ID = "test-session-456";

    @BeforeEach
    void setUp() {
        // 初始化测试房型
        testRoomType = new RoomType();
        testRoomType.setId(1L);
        testRoomType.setName("大床房");
        testRoomType.setPrice(new BigDecimal("200.00"));
        testRoomType.setTotalRooms(10);

        // 初始化有效日期（明天入住，后天离店）
        validCheckIn = LocalDate.now().plusDays(1);
        validCheckOut = validCheckIn.plusDays(1);
    }

    /**
     * 测试：房型不存在时返回错误提示
     */
    @Test
    void bookRoom_roomTypeNotFound_returnsError() {
        // 模拟：查询不到对应房型
        when(roomTypeRepository.findByNameContainingIgnoreCase("未知房型")).thenReturn(Optional.empty());

        // 执行测试
        String result = bookingService.bookRoom(
                TEST_USER_ID,
                TEST_SESSION_ID,
                "未知房型",
                validCheckIn,
                validCheckOut,
                1
        );

        // 断言
        assertEquals("抱歉，我们没有“未知房型”这种房型，请选择大床房、双床房或套房。", result);
        verify(roomTypeRepository, times(1)).findByNameContainingIgnoreCase("未知房型");
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    /**
     * 测试：入住日期早于今天返回错误提示
     */
    @Test
    void bookRoom_checkInBeforeToday_returnsError() {
        // 模拟：能查询到房型
        when(roomTypeRepository.findByNameContainingIgnoreCase("大床房")).thenReturn(Optional.of(testRoomType));

        // 构造无效日期（昨天）
        LocalDate invalidCheckIn = LocalDate.now().minusDays(1);

        // 执行测试
        String result = bookingService.bookRoom(
                TEST_USER_ID,
                TEST_SESSION_ID,
                "大床房",
                invalidCheckIn,
                validCheckOut,
                1
        );

        // 断言
        assertEquals("入住日期不能早于今天。", result);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    /**
     * 测试：离店日期小于入住+1天返回错误提示
     */
    @Test
    void bookRoom_checkOutBeforeCheckInPlus1Day_returnsError() {
        // 模拟：能查询到房型
        when(roomTypeRepository.findByNameContainingIgnoreCase("大床房")).thenReturn(Optional.of(testRoomType));

        // 构造无效离店日期（和入住日期同一天）
        LocalDate invalidCheckOut = validCheckIn;

        // 执行测试
        String result = bookingService.bookRoom(
                TEST_USER_ID,
                TEST_SESSION_ID,
                "大床房",
                validCheckIn,
                invalidCheckOut,
                1
        );

        // 断言
        assertEquals("离店日期必须至少比入住日期晚一天。", result);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    /**
     * 测试：房间不足时返回错误提示
     */
    @Test
    void bookRoom_insufficientRooms_returnsError() {
        // 模拟：能查询到房型
        when(roomTypeRepository.findByNameContainingIgnoreCase("大床房")).thenReturn(Optional.of(testRoomType));
        // 模拟：冲突房间数=10（总房间数10，剩余0）
        when(reservationRepository.countConflictingReservations(1L, validCheckIn, validCheckOut)).thenReturn(10);

        // 执行测试（预订1间）
        String result = bookingService.bookRoom(
                TEST_USER_ID,
                TEST_SESSION_ID,
                "大床房",
                validCheckIn,
                validCheckOut,
                1
        );

        // 断言
        assertEquals("抱歉，所选时间段内“大床房”只剩 0 间，无法预订 1 间。", result);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    /**
     * 测试：预订成功场景
     */


    /**
     * 测试：查询无预订记录返回提示
     */
    @Test
    void queryReservation_noReservations_returnsEmptyPrompt() {
        // 模拟：无任何预订记录
        when(reservationRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        // 执行测试
        String result = bookingService.queryReservation(TEST_USER_ID);

        // 断言
        assertEquals("您当前没有有效预订。", result);
    }

    /**
     * 测试：查询有预订记录返回正确信息
     */
    @Test
    void queryReservation_hasReservations_returnsReservationInfo() {
        // 构造测试预订记录
        Reservation testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setUserId(TEST_USER_ID);
        testReservation.setSessionId(TEST_SESSION_ID);
        testReservation.setRoomTypeId(1L);
        testReservation.setCheckInDate(validCheckIn);
        testReservation.setCheckOutDate(validCheckOut);
        testReservation.setRoomCount(1);
        testReservation.setStatus("BOOKED");
        testReservation.setCreatedAt(LocalDateTime.now());

        // 模拟：查询到该用户的预订记录
        when(reservationRepository.findAll()).thenReturn(java.util.Collections.singletonList(testReservation));
        // 模拟：查询房型名称
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(testRoomType));

        // 执行测试
        String result = bookingService.queryReservation(TEST_USER_ID);

        // 断言
        assertTrue(result.startsWith("您的订单号：1，房型：大床房，入住：" + validCheckIn + "，离店：" + validCheckOut + "，状态：BOOKED"));
    }
}