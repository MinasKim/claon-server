package coLaon.ClaonBack.post.dto.validator;

import coLaon.ClaonBack.post.dto.ClimbingHistoryRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ClimbingHistorySizeValidator implements ConstraintValidator<ClimbingHistorySize, List<ClimbingHistoryRequestDto>> {
    @Override
    public boolean isValid(
            List<ClimbingHistoryRequestDto> value,
            ConstraintValidatorContext context
    ) {
        if (value == null) return true;
        int size = value.stream().map(ClimbingHistoryRequestDto::getClimbingCount).reduce(0, Integer::sum);
        return size <= 10 && size > 0;
    }
}
