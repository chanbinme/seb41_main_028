package challenge.server.user.repository;

import challenge.server.user.entity.EmailVerification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static challenge.server.user.entity.QEmailVerification.emailVerification;

@Repository
@RequiredArgsConstructor
public class EmailVerificationCustomRepositoryImpl implements EmailVerificationCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Optional<EmailVerification> findValidVerificationByEmail(String email, String verificationToken, LocalDateTime currentTime) {
        EmailVerification result = jpaQueryFactory
                .selectFrom(emailVerification)
                .where(emailVerification.email.eq(email),
                        emailVerification.verificationToken.eq(verificationToken),
                        emailVerification.expiryTime.goe(currentTime), // 만료 시간 >= 현재 시간
                        emailVerification.isExpired.eq(false))
                .fetchFirst();

        return Optional.ofNullable(result);
    }
}