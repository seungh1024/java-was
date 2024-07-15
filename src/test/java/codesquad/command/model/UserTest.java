package codesquad.command.model;

import codesquad.db.user.MemberRepository;
import codesquad.db.user.Member;
import codesquad.exception.CustomException;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {


    String userId = "testId";
    String userPass = "testPass";
    String userName = "testName";
    String userEmail = "testEmail@naver.com";
    int corePoolSize = 10;
    int maxPoolSize = 50;
    int keepAliveTime = 10;
    int queueCapacity = 10;

    @BeforeEach
    void setUp(){
        MemberRepository.getInstance().deleteUserInfo(userId);
    }

    @Nested
    @DisplayName("유저 클래스 동시성 테스트")
    class concurrency_test {

        @Test
        @DisplayName("같은 아이디의 사용자가 있으면, 회원 가입 시에 예외를 반환한다.")
        void request_with_already_existed_userId() throws InterruptedException {
            // given
            MemberRepository user = MemberRepository.getInstance();
            Member userInfo = new Member(userId, userPass, userName, userEmail);
            user.addUserInfo(userInfo);
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
            CountDownLatch countDownLatch = new CountDownLatch(queueCapacity);
            AtomicInteger exceptionCount = new AtomicInteger(0);
            AtomicInteger otherExceptionCount = new AtomicInteger(0);


            // when
            for (int i = 0; i < queueCapacity; i++) {
                threadPoolExecutor.execute(() -> {
                    try {
                        user.addUserInfo(new Member(userId, userPass, userName, userEmail));

                    } catch (CustomException exception) {
                        exceptionCount.getAndIncrement();
                    } catch (Exception exception) {
                        otherExceptionCount.getAndIncrement();
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();

            // then
            assertEquals(exceptionCount.get(), queueCapacity);
            assertEquals(otherExceptionCount.get(), 0);
            assertEquals(user.getUserInfo(userId), userInfo);
        }

        @Test
        @DisplayName("다른 아이디로 회원가입 하면 정상적으로 사용자 정보가 저장된다.")
        void request_with_different_user_id() throws InterruptedException {
            // given
            MemberRepository user = MemberRepository.getInstance();
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
            CountDownLatch countDownLatch = new CountDownLatch(queueCapacity);
            AtomicInteger exceptionCount = new AtomicInteger(0);
            AtomicInteger otherExceptionCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);


            // when
            for (int i = 0; i < queueCapacity; i++) {
                int idx = i;
                threadPoolExecutor.execute(() -> {
                    try {
                        user.addUserInfo(new Member(userId+idx, userPass, userName, userEmail));

                    } catch (CustomException exception) {
                        exceptionCount.getAndIncrement();
                    } catch (Exception exception) {
                        otherExceptionCount.getAndIncrement();
                    } finally {
                        countDownLatch.countDown();
                        successCount.getAndIncrement();
                    }
                });
            }

            countDownLatch.await();

            // then
            assertEquals(exceptionCount.get(), 0);
            assertEquals(otherExceptionCount.get(), 0);
            assertEquals(successCount.get(), queueCapacity);
        }
    }
}