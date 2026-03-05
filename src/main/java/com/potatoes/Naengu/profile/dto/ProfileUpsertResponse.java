package com.potatoes.Naengu.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileUpsertResponse (
        @Schema(example = "3")
        Long profileId,
        @Schema(example = "false")
        boolean isNew
){
}
