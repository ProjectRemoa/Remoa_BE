package Remoa.BE.Web.Member.Dto.Res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResFollowerAndFollowingDto {

    private Integer follower;

    private Integer following;

    Boolean isFollow;
}
