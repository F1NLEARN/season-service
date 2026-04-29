package com.finlearn.seasonservice.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterParticipantRequest {

    @NotNull(message = "카테고리 목록은 null일 수 없습니다.")
    @NotEmpty(message = "통과한 카테고리 목록은 비어있을 수 없습니다.")
    private List<@NotNull(message = "카테고리 항목은 null일 수 없습니다.") String> passedCategories;
}
