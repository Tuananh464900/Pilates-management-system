package vn.n06g12.pilates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.n06g12.pilates.booking.entity.Booking;
import vn.n06g12.pilates.booking.entity.BookingStatus;
import vn.n06g12.pilates.booking.repository.BookingRepository;
import vn.n06g12.pilates.booking.service.BookingService;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.membership.dto.ConfirmPaymentRequest;
import vn.n06g12.pilates.membership.entity.*;
import vn.n06g12.pilates.membership.repository.MemberMembershipRepository;
import vn.n06g12.pilates.membership.repository.MembershipPackageRepository;
import vn.n06g12.pilates.membership.repository.PaymentRepository;
import vn.n06g12.pilates.membership.service.PaymentService;
import vn.n06g12.pilates.pilatesclass.entity.ClassLevel;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.entity.PilatesClass;
import vn.n06g12.pilates.pilatesclass.repository.PilatesClassRepository;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.repository.RoomRepository;
import vn.n06g12.pilates.session.dto.CancelSessionRequest;
import vn.n06g12.pilates.session.dto.CreateSessionRequest;
import vn.n06g12.pilates.session.entity.Session;
import vn.n06g12.pilates.session.entity.SessionStatus;
import vn.n06g12.pilates.session.repository.SessionRepository;
import vn.n06g12.pilates.session.service.SessionService;
import vn.n06g12.pilates.user.entity.Role;
import vn.n06g12.pilates.user.entity.User;
import vn.n06g12.pilates.user.entity.UserStatus;
import vn.n06g12.pilates.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencySafetyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberMembershipRepository membershipRepository;

    @Autowired
    private MembershipPackageRepository packageRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PilatesClassRepository classRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User member;
    private User trainer;
    private PilatesClass pilatesClass;
    private Room room1;
    private Room room2;
    private MembershipPackage pkg;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        paymentRepository.deleteAll();
        membershipRepository.deleteAll();
        sessionRepository.deleteAll();
        classRepository.deleteAll();
        roomRepository.deleteAll();
        packageRepository.deleteAll();
        userRepository.deleteAll();

        member = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("member.concurrency@pilates.vn")
                .role(Role.MEMBER)
                .status(UserStatus.ACTIVE)
                .build());

        trainer = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("trainer.concurrency@pilates.vn")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build());

        pilatesClass = classRepository.save(PilatesClass.builder()
                .name("Pilates Concurrency Safety Class")
                .level(ClassLevel.BEGINNER)
                .defaultCapacity(10)
                .status(ClassStatus.ACTIVE)
                .build());

        room1 = roomRepository.save(Room.builder()
                .name("Room Concurrency 1")
                .capacity(10)
                .status(RoomStatus.ACTIVE)
                .build());

        room2 = roomRepository.save(Room.builder()
                .name("Room Concurrency 2")
                .capacity(10)
                .status(RoomStatus.ACTIVE)
                .build());

        pkg = packageRepository.save(MembershipPackage.builder()
                .name("Gói 10 Buổi An Toàn Tương Tranh")
                .price(new BigDecimal("2000000"))
                .durationDays(30)
                .totalSessions(10)
                .status(PackageStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("UC4.2 Concurrency - Một Hội viên gửi đồng thời 2 request đặt 2 buổi tập trùng khung giờ -> Chỉ 1 request thành công, không bị trùng lịch")
    void testConcurrentBooking_OverlappingSchedule_OnlyOneSucceeds() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(5)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        // Tạo 2 Session khác nhau nhưng diễn ra CÙNG KHUNG GIỜ (Session A ở Room 1, Session B ở Room 2)
        Instant start = now.plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        Session sessionA = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room1)
                .trainer(trainer)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build());

        Session sessionB = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room2)
                .trainer(trainer)
                .startTime(start.plus(15, ChronoUnit.MINUTES)) // Trùng giờ với Session A
                .endTime(end.plus(15, ChronoUnit.MINUTES))
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // Luồng 1 đặt Session A
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBooking(member.getId(), sessionA.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        // Luồng 2 đặt Session B
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBooking(member.getId(), sessionB.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        // Kích hoạt đồng thời cả 2 luồng
        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "Thời gian chạy vượt quá 10 giây");
        executor.shutdown();

        // Kiểm tra kết quả
        assertEquals(1, successCount.get(), "Chỉ đúng 1 request được phép thành công");
        assertEquals(1, failCount.get(), "Request còn lại phải bị từ chối do xung đột lịch");

        // Kiểm tra số lượt gói tập chỉ bị trừ đúng 1 lượt
        MemberMembership refreshedMem = membershipRepository.findById(membership.getId()).orElseThrow();
        assertEquals(4, refreshedMem.getRemainingSessions(), "Lượt tập chỉ bị trừ đúng 1 lần (từ 5 về 4)");
    }

    @Test
    @DisplayName("UC4.4 Concurrency - Hai request đồng thời yêu cầu hủy CÙNG MỘT Booking -> Chỉ 1 request hoàn lượt, không bao giờ hoàn lượt 2 lần")
    void testConcurrentCancelBooking_SameBooking_OnlyOneRefund() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(3)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        // Buổi tập diễn ra sau 3 ngày (> 2 giờ -> đủ điều kiện hoàn lượt)
        Instant start = now.plus(3, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room1)
                .trainer(trainer)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .session(session)
                .member(member)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    bookingService.cancelBooking(member.getId(), booking.getId(), "Kiểm thử tương tranh hủy buổi");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Kích hoạt đồng thời cả 2 luồng hủy
        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Kiểm tra: Chỉ 1 request được hủy thành công
        assertEquals(1, successCount.get(), "Chỉ đúng 1 request hủy được chấp nhận");
        assertEquals(1, failCount.get(), "Request thứ hai phải bị từ chối vì booking đã chuyển CANCELED");

        // Kiểm tra số lượt gói tập chỉ được hoàn đúng 1 lượt (từ 3 lên 4, KHÔNG PHẢI 5)
        MemberMembership refreshedMem = membershipRepository.findById(membership.getId()).orElseThrow();
        assertEquals(4, refreshedMem.getRemainingSessions(), "Gói tập chỉ được hoàn đúng 1 lượt (3 + 1 = 4)");

        // Kiểm tra bookedCount của buổi tập giảm về 0
        Session refreshedSession = sessionRepository.findById(session.getId()).orElseThrow();
        assertEquals(0, refreshedSession.getBookedCount(), "bookedCount của buổi tập phải giảm từ 1 về 0");
    }

    @Test
    @DisplayName("UC3.3 Concurrency - Hai Payment khác nhau của cùng một Member được xác nhận đồng thời -> Tuần tự hóa, tối đa 1 gói ACTIVE")
    void testConcurrentPaymentConfirmation_SameMember_SerializesActivation() throws Exception {
        // Tạo 2 Payment UNPAID của cùng một hội viên
        Payment payment1 = paymentRepository.save(Payment.builder()
                .member(member)
                .membershipPackage(pkg)
                .amount(pkg.getPrice())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .transactionCode("TX-CONCURRENCY-001")
                .build());

        Payment payment2 = paymentRepository.save(Payment.builder()
                .member(member)
                .membershipPackage(pkg)
                .amount(pkg.getPrice())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.UNPAID)
                .transactionCode("TX-CONCURRENCY-002")
                .build());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                paymentService.confirmPayment(payment1.getId(), ConfirmPaymentRequest.builder()
                        .paymentMethod(PaymentMethod.BANK_TRANSFER)
                        .note("Admin confirm P1")
                        .build());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                paymentService.confirmPayment(payment2.getId(), ConfirmPaymentRequest.builder()
                        .paymentMethod(PaymentMethod.CASH)
                        .note("Admin confirm P2")
                        .build());
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        // Kích hoạt đồng thời cả 2 luồng xác nhận
        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // Cả 2 đều thành công vì cùng loại gói -> 1 gói được kích hoạt và 1 gói được gia hạn (cộng dồn)
        assertEquals(2, successCount.get());

        // Kiểm tra số gói ACTIVE của Member: Chỉ đúng 1 gói ACTIVE duy nhất!
        long activeCount = membershipRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .count();
        assertEquals(1, activeCount, "Hội viên chỉ có đúng 1 gói ACTIVE duy nhất (gói thứ hai đã được cộng dồn)");

        // Kiểm tra tổng số lượt: 10 + 10 = 20 lượt
        MemberMembership activeMem = membershipRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .findFirst().orElseThrow();
        assertEquals(20, activeMem.getRemainingSessions(), "Tổng số lượt tập sau khi cộng dồn gia hạn là 20");
    }

    @Test
    @DisplayName("P1 Concurrency - Đặt lịch đồng thời với gia hạn gói -> Cả hai thành công, không mất cập nhật số lượt")
    void testConcurrentBookingAndRenewal_NoLostUpdate() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(5)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Instant start = now.plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room1)
                .trainer(trainer)
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build());

        Payment payment = paymentRepository.save(Payment.builder()
                .member(member)
                .membershipPackage(pkg)
                .amount(pkg.getPrice())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .transactionCode("TX-RENEW-CONCURRENT-01")
                .build());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // Luồng 1: Đặt lịch tập (trừ 1 lượt)
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBooking(member.getId(), session.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                // error
            } finally {
                finishLatch.countDown();
            }
        });

        // Luồng 2: Gia hạn gói tập (cộng 10 lượt)
        executor.submit(() -> {
            try {
                startLatch.await();
                paymentService.confirmPayment(payment.getId(), ConfirmPaymentRequest.builder()
                        .paymentMethod(PaymentMethod.BANK_TRANSFER)
                        .note("Renewal confirm")
                        .build());
                successCount.incrementAndGet();
            } catch (Exception e) {
                // error
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(2, successCount.get(), "Cả đặt lịch và gia hạn đều phải thành công");

        MemberMembership refreshed = membershipRepository.findById(membership.getId()).orElseThrow();
        // Ban đầu 5 lượt - 1 lượt đặt + 10 lượt gia hạn = 14 lượt
        assertEquals(14, refreshed.getRemainingSessions(), "Tổng số lượt phải là 14 (5 - 1 + 10), không bị lost update");
    }

    @Test
    @DisplayName("P1 Concurrency - Admin hủy đồng thời 2 buổi tập của cùng một hội viên -> Cả hai hoàn lượt an toàn, không bị lost update")
    void testConcurrentCancelSession_SameMemberMultipleSessions_AllRefunded() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(2)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(29, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Instant start1 = now.plus(2, ChronoUnit.DAYS);
        Session session1 = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room1)
                .trainer(trainer)
                .startTime(start1)
                .endTime(start1.plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        Instant start2 = now.plus(3, ChronoUnit.DAYS);
        Session session2 = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .room(room2)
                .trainer(trainer)
                .startTime(start2)
                .endTime(start2.plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        bookingRepository.save(Booking.builder()
                .member(member)
                .session(session1)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        bookingRepository.save(Booking.builder()
                .member(member)
                .session(session2)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // Luồng 1: Admin hủy Session 1
        executor.submit(() -> {
            try {
                startLatch.await();
                sessionService.cancelSession(session1.getId(), CancelSessionRequest.builder().reason("Bảo trì phòng 1").build());
                successCount.incrementAndGet();
            } catch (Exception e) {
                // error
            } finally {
                finishLatch.countDown();
            }
        });

        // Luồng 2: Admin hủy Session 2
        executor.submit(() -> {
            try {
                startLatch.await();
                sessionService.cancelSession(session2.getId(), CancelSessionRequest.builder().reason("Bảo trì phòng 2").build());
                successCount.incrementAndGet();
            } catch (Exception e) {
                // error
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(2, successCount.get(), "Cả 2 lệnh hủy buổi tập đều phải thành công");

        MemberMembership refreshed = membershipRepository.findById(membership.getId()).orElseThrow();
        // Ban đầu 2 lượt + 1 lượt hoàn từ session 1 + 1 lượt hoàn từ session 2 = 4 lượt
        assertEquals(4, refreshed.getRemainingSessions(), "Tổng số lượt phải là 4 (2 + 1 + 1), không bị lost update khi hoàn lượt đồng thời");

        Session refSession1 = sessionRepository.findById(session1.getId()).orElseThrow();
        Session refSession2 = sessionRepository.findById(session2.getId()).orElseThrow();
        assertEquals(0, refSession1.getBookedCount(), "bookedCount session 1 phải về 0");
        assertEquals(0, refSession2.getBookedCount(), "bookedCount session 2 phải về 0");
    }

    @Test
    @DisplayName("Lỗi 3 Concurrency - Hai Admin đồng thời tạo buổi tập trùng phòng và khung giờ -> Chỉ 1 request thành công, 1 request bị từ chối do xung đột lịch")
    void testConcurrentCreateSession_SameRoom_OnlyOneSucceeds() throws Exception {
        Instant now = Instant.now();
        Instant start = now.plus(4, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        User trainer2 = userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("trainer2.concurrency@pilates.vn")
                .role(Role.TRAINER)
                .status(UserStatus.ACTIVE)
                .build());

        // Cả 2 request đều dùng room1 và cùng khung giờ start - end, nhưng với 2 HLV khác nhau
        CreateSessionRequest req1 = CreateSessionRequest.builder()
                .classId(pilatesClass.getId())
                .roomId(room1.getId())
                .trainerId(trainer.getId())
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .build();

        CreateSessionRequest req2 = CreateSessionRequest.builder()
                .classId(pilatesClass.getId())
                .roomId(room1.getId())
                .trainerId(trainer2.getId())
                .startTime(start)
                .endTime(end)
                .capacity(10)
                .build();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                startLatch.await();
                sessionService.createSession(req1);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                sessionService.createSession(req2);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ đúng 1 buổi tập được phép tạo thành công");
        assertEquals(1, failCount.get(), "Buổi tập thứ hai phải bị từ chối do xung đột lịch phòng");

        // Kiểm tra database chỉ có duy nhất 1 buổi tập được lưu tại room1
        long sessionCountInRoom1 = sessionRepository.findAll().stream()
                .filter(s -> s.getRoom().getId().equals(room1.getId()) && s.getStatus() == SessionStatus.SCHEDULED)
                .count();
        assertEquals(1, sessionCountInRoom1, "Database chỉ lưu duy nhất 1 buổi tập trong khung giờ tại room1");
    }

    @Test
    @DisplayName("Lỗi A Concurrency - Member đặt buổi mới và hủy buổi cũ để hoàn lượt đồng thời -> Khóa gói ACTIVE, không mất cập nhật số lượt")
    void testConcurrentBookingAndRefund_SameActivePackage_NoLostUpdate() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(5)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(30, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        // Session 1: diễn ra sau 3 ngày (để hủy hoàn lượt >= 2h)
        Session session1 = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer)
                .room(room1)
                .startTime(now.plus(3, ChronoUnit.DAYS))
                .endTime(now.plus(3, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        Booking existingBooking = bookingRepository.save(Booking.builder()
                .member(member)
                .session(session1)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        // Session 2: diễn ra sau 4 ngày (để đặt buổi mới)
        Session session2 = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer)
                .room(room2)
                .startTime(now.plus(4, ChronoUnit.DAYS))
                .endTime(now.plus(4, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(0)
                .status(SessionStatus.SCHEDULED)
                .build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        // Thread 1: Đặt chỗ session 2 (trừ 1 lượt)
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.createBooking(member.getId(), session2.getId());
            } catch (Exception ignored) {
            } finally {
                finishLatch.countDown();
            }
        });

        // Thread 2: Hủy booking của session 1 (hoàn 1 lượt)
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.cancelBooking(member.getId(), existingBooking.getId(), "Hủy thử nghiệm");
            } catch (Exception ignored) {
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        MemberMembership updatedMem = membershipRepository.findById(membership.getId()).orElseThrow();
        assertEquals(5, updatedMem.getRemainingSessions(), "Số lượt tập đúng phải là 5 (ban đầu 5 - 1 đặt + 1 hoàn = 5)");
    }

    @Test
    @DisplayName("Lỗi B Concurrency - Admin hủy buổi và Member hủy booking diễn ra đồng thời -> Thống nhất thứ tự khóa, không bao giờ deadlock")
    void testConcurrentAdminCancelSessionAndMemberCancelBooking_NoDeadlock() throws Exception {
        Instant now = Instant.now();
        MemberMembership membership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(2)
                .startDate(now.minus(1, ChronoUnit.DAYS))
                .endDate(now.plus(30, ChronoUnit.DAYS))
                .status(MembershipStatus.ACTIVE)
                .build());

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer)
                .room(room1)
                .startTime(now.plus(2, ChronoUnit.DAYS))
                .endTime(now.plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .member(member)
                .session(session)
                .membership(membership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        // Thread 1: Admin hủy buổi tập
        executor.submit(() -> {
            try {
                startLatch.await();
                sessionService.cancelSession(session.getId(), new CancelSessionRequest("Admin hủy buổi thử nghiệm"));
            } catch (Exception ignored) {
            } finally {
                finishLatch.countDown();
            }
        });

        // Thread 2: Member hủy booking của chính buổi tập đó
        executor.submit(() -> {
            try {
                startLatch.await();
                bookingService.cancelBooking(member.getId(), booking.getId(), "Member hủy thử nghiệm");
            } catch (Exception ignored) {
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS), "Cả 2 thread phải kết thúc trong vòng 10s không bị deadlock");
        executor.shutdown();

        // Không bị deadlock và số lượt tập chỉ được hoàn đúng 1 lần (2 + 1 = 3)
        MemberMembership updatedMem = membershipRepository.findById(membership.getId()).orElseThrow();
        assertEquals(3, updatedMem.getRemainingSessions(), "Số lượt sau hoàn chỉ tăng đúng 1 lần, không hoàn đúp");

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CANCELED, updatedBooking.getStatus());
        assertTrue(Boolean.TRUE.equals(updatedBooking.getRefunded()));
    }

    @Test
    @DisplayName("Lỗi C - Không cho chuyển sang gói khác loại khi gói cũ còn các booking chưa diễn ra có khả năng phát sinh hoàn lượt")
    void testPreventPurchaseDifferentPackageType_WhenOldPackageHasPendingBookings() {
        Instant now = Instant.now();
        MemberMembership oldMembership = membershipRepository.save(MemberMembership.builder()
                .member(member)
                .membershipPackage(pkg)
                .remainingSessions(0)
                .startDate(now.minus(10, ChronoUnit.DAYS))
                .endDate(now.plus(20, ChronoUnit.DAYS))
                .status(MembershipStatus.EXHAUSTED)
                .build());

        Session session = sessionRepository.save(Session.builder()
                .pilatesClass(pilatesClass)
                .trainer(trainer)
                .room(room1)
                .startTime(now.plus(5, ChronoUnit.DAYS))
                .endTime(now.plus(5, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .capacity(10)
                .bookedCount(1)
                .status(SessionStatus.SCHEDULED)
                .build());

        bookingRepository.save(Booking.builder()
                .member(member)
                .session(session)
                .membership(oldMembership)
                .status(BookingStatus.CONFIRMED)
                .refunded(false)
                .build());

        MembershipPackage differentPkg = packageRepository.save(MembershipPackage.builder()
                .name("Gói Khác Loại VIP 20 Buổi")
                .price(new BigDecimal("4000000"))
                .durationDays(60)
                .totalSessions(20)
                .status(PackageStatus.ACTIVE)
                .build());

        Payment payment = paymentRepository.save(Payment.builder()
                .transactionCode("TXN-TEST-DIFF-001")
                .member(member)
                .membershipPackage(differentPkg)
                .amount(differentPkg.getPrice())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.UNPAID)
                .build());

        // Kích hoạt gói khác loại phải bị chặn vì gói cũ còn booking CONFIRMED trong tương lai
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            paymentService.confirmPayment(payment.getId(), new ConfirmPaymentRequest());
        });
        assertTrue(ex.getMessage().contains("chưa diễn ra có khả năng phát sinh hoàn lượt"));
    }

    @Test
    @DisplayName("Lỗi D - Tạo buổi tập có sức chứa vượt quá sức chứa phòng tập -> Bị từ chối với BusinessException")
    void testCreateSession_ExceedingRoomCapacity_ThrowsBusinessException() {
        Instant now = Instant.now();
        // Room capacity = 10
        CreateSessionRequest req = new CreateSessionRequest();
        req.setClassId(pilatesClass.getId());
        req.setTrainerId(trainer.getId());
        req.setRoomId(room1.getId());
        req.setStartTime(now.plus(10, ChronoUnit.DAYS));
        req.setEndTime(now.plus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS));
        req.setCapacity(25); // 25 > 10

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            sessionService.createSession(req);
        });
        assertTrue(ex.getMessage().contains("không được vượt quá sức chứa tối đa của phòng tập"));
    }
}
