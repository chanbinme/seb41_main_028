package challenge.server.review.service;

import challenge.server.exception.BusinessLogicException;
import challenge.server.exception.ExceptionCode;
import challenge.server.firebase.FCMService;
import challenge.server.habit.service.HabitService;
import challenge.server.review.dto.ReviewDto;
import challenge.server.review.entity.Review;
import challenge.server.review.mapper.ReviewMapper;
import challenge.server.review.repository.ReviewRepository;
import challenge.server.utils.CustomBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomBeanUtils<Review> beanUtils;
    private final HabitService habitService;
    private final ReviewMapper mapper;
    private final FCMService fcmService;

    public Review createReview(Review review) {
        verfiyExistReview(review.getHabit().getHabitId(), review.getUser().getUserId());
        fcmService.sendReviewNotice(review.getHabit().getHost(),
                review.getBody(), review.getHabit().getHabitId(), review.getUser().getUsername());   // 리뷰가 달린 습관의 작성자에게 알림 전송

        return reviewRepository.save(review);
    }

    public ReviewDto.Response updateReview(Review review) {
        Review findReview = findVerifiedReview(review.getReviewId());
        beanUtils.copyNonNullProperties(review, findReview);
        reviewRepository.save(findReview);
        habitService.calcAvgScore(findReview.getHabit().getHabitId());

//        Optional.ofNullable(review.getBody())
//                .ifPresent(body -> findReview.setBody(body));
//        Optional.ofNullable(review.getScore())
//                .ifPresent(score -> findReview.setScore(score));

        return mapper.toDto(findReview);
    }

    public Review findReview(Long reviewId) {
        return findVerifiedReview(reviewId);
    }

    public List<Review> findAll(Long lastReviewId, int size) {
        return reviewRepository.findAllNoOffset(lastReviewId, size);
    }

    public List<ReviewDto.Response> findAllByHabit(Long lastReviewId, Long habitId, int size) {
        List<Review> reviews = reviewRepository.findAllByHabitHabitId(lastReviewId, habitId, size);
        return mapper.toDtos(reviews);
    }

    public void deleteReview(Long reviewId) {
        findVerifiedReview(reviewId);
        reviewRepository.deleteById(reviewId);
    }

    public void verfiyExistReview(Long habitId, Long userId) {
        Optional<Review> optionalReview = reviewRepository.findByHabitHabitIdAndUserUserId(habitId, userId);
        if (optionalReview.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.REVIEW_EXISTS);
        }
    }

    public Review findVerifiedReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.REVIEW_NOT_FOUND));
    }
}
