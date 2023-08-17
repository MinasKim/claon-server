package com.claon.center.service;

import com.claon.center.domain.*;
import com.claon.center.dto.*;
import com.claon.center.repository.CenterRepository;
import com.claon.center.repository.ReviewRepository;
import com.claon.center.repository.ReviewRepositorySupport;
import com.claon.center.common.domain.PaginationFactory;
import com.claon.center.common.exception.BadRequestException;
import com.claon.center.common.exception.ErrorCode;
import com.claon.center.common.exception.UnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class CenterReviewServiceTest {
    @Mock
    CenterRepository centerRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    ReviewRepositorySupport reviewRepositorySupport;

    @Spy
    PaginationFactory paginationFactory = new PaginationFactory();

    @InjectMocks
    CenterReviewService centerReviewService;

    private final String USER_ID = "USER_ID";
    private final String USER2_ID = "USER2_ID";
    private Center center;
    private CenterReview review1, review2;

    @BeforeEach
    void setUp() {
        this.center = Center.of(
                "test",
                "test",
                "010-1234-1234",
                "https://test.com",
                "https://instagram.com/test",
                "https://youtube.com/channel/test",
                List.of(new CenterImg("img test")),
                List.of(new OperatingTime("매일", "10:00", "23:00")),
                "facilities test",
                List.of(new Charge(List.of(new ChargeElement("자유 패키지", "330,000")), "charge image")),
                "hold info img test"
        );
        ReflectionTestUtils.setField(this.center, "id", "center id");

        this.review1 = CenterReview.of(5, "testContent1", USER_ID, this.center);
        ReflectionTestUtils.setField(this.review1, "id", "review1Id");
        ReflectionTestUtils.setField(this.review1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(this.review1, "updatedAt", LocalDateTime.now());
        this.review2 = CenterReview.of(4, "testContent2", USER_ID, this.center);
        ReflectionTestUtils.setField(this.review2, "id", "review2Id");
        ReflectionTestUtils.setField(this.review2, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(this.review2, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("Success case for create center review")
    void successCreateReview() {
        try (MockedStatic<CenterReview> reviewMockedStatic = mockStatic(CenterReview.class)) {
            // given
            ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(5, "testContent");

            given(this.centerRepository.findById("testCenterId")).willReturn(Optional.of(center));

            reviewMockedStatic.when(() -> CenterReview.of(5, "testContent", USER_ID, this.center)).thenReturn(this.review1);

            given(this.reviewRepository.save(this.review1)).willReturn(this.review1);

            // when
            ReviewResponseDto reviewResponseDto = this.centerReviewService.createReview(USER_ID, "testCenterId", reviewCreateRequestDto);

            // then
            assertThat(reviewResponseDto)
                    .isNotNull()
                    .extracting("reviewId", "content")
                    .contains(this.review1.getId(), this.review1.getContent());
        }
    }

    @Test
    @DisplayName("Failure case for create center review for existing own review in center")
    void failureCreateReview_alreadyExist() {
        ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(5, "testContent");

        given(this.centerRepository.findById("center id")).willReturn(Optional.of(center));
        given(this.reviewRepository.findByUserIdAndCenterId(USER_ID, center.getId())).willReturn(Optional.of(review1));

        // when
        final BadRequestException ex = Assertions.assertThrows(
                BadRequestException.class,
                () -> this.centerReviewService.createReview(USER_ID, "center id", reviewCreateRequestDto)
        );

        // then
        assertThat(ex)
                .extracting("errorCode", "message")
                .contains(ErrorCode.ROW_ALREADY_EXIST, "이미 작성된 리뷰가 있습니다.");
    }

    @Test
    @DisplayName("Success case for update review")
    void successUpdateReview() {
        // given
        ReviewUpdateRequestDto reviewUpdateRequestDto = new ReviewUpdateRequestDto(1, "updateContent");

        given(this.reviewRepository.findById("review1Id")).willReturn(Optional.of(review1));
        given(this.reviewRepository.save(this.review1)).willReturn(this.review1);

        // when
        ReviewResponseDto reviewResponseDto = this.centerReviewService.updateReview(USER_ID, "review1Id", reviewUpdateRequestDto);

        // then
        assertThat(reviewResponseDto)
                .isNotNull()
                .extracting("content", "reviewId")
                .contains("updateContent", "review1Id");
    }

    @Test
    @DisplayName("Failure case for update review because update by other user")
    void failUpdateReview_Unauthorized() {
        // given
        ReviewUpdateRequestDto reviewUpdateRequestDto = new ReviewUpdateRequestDto(1, "updateContent");

        given(this.reviewRepository.findById("reviewId")).willReturn(Optional.of(review1));

        // when
        final UnauthorizedException ex = Assertions.assertThrows(
                UnauthorizedException.class,
                () -> this.centerReviewService.updateReview(USER2_ID, "reviewId", reviewUpdateRequestDto)
        );

        // then
        assertThat(ex)
                .extracting("errorCode", "message")
                .contains(ErrorCode.NOT_ACCESSIBLE, "접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("Success case for delete review")
    void successDeleteReview() {
        // given
        given(this.reviewRepository.findById("review1Id")).willReturn(Optional.of(review1));

        // when
        this.centerReviewService.deleteReview(USER_ID, "review1Id");

        // then
        org.assertj.core.api.Assertions.assertThat(this.reviewRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Failure case for delete review because update by other user")
    void failDeleteReview_Unauthorized() {
        // given
        given(this.reviewRepository.findById("reviewId")).willReturn(Optional.of(review1));

        // when
        final UnauthorizedException ex = Assertions.assertThrows(
                UnauthorizedException.class,
                () -> this.centerReviewService.deleteReview(USER2_ID, "reviewId")
        );

        // then
        assertThat(ex)
                .extracting("errorCode", "message")
                .contains(ErrorCode.NOT_ACCESSIBLE, "접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("Success case for find center review")
    void successFindReview() {
        //given
        Pageable pageable = PageRequest.of(0, 2);
        Page<CenterReview> centerReviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);

        given(this.centerRepository.findById("centerId")).willReturn(Optional.of(center));
        given(this.reviewRepository.findByUserIdAndCenterId(USER_ID, center.getId())).willReturn(Optional.of(review1));
        given(this.reviewRepositorySupport.findByCenterExceptBlockUserAndSelf(center.getId(), USER_ID, pageable)).willReturn(centerReviewPage);

        //when
        ReviewBundleFindResponseDto reviewBundleFindResponseDto = this.centerReviewService.findReview(USER_ID, "centerId", pageable);

        // then
        org.assertj.core.api.Assertions.assertThat(reviewBundleFindResponseDto.getOtherReviewsPagination().getResults())
                .isNotNull()
                .extracting(ReviewFindResponseDto::getReviewId, ReviewFindResponseDto::getRank)
                .contains(
                        tuple(review1.getId(), review1.getRank()),
                        tuple(review2.getId(), review2.getRank())
                );
    }
}
