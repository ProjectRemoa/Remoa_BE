package Remoa.BE.Member.Form.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileForm {

    @NotBlank(message = "닉네임은 비어있을 수 없습니다.")
    private String nickname;

    private String phoneNumber;

    private String university;

    private String oneLineIntroduction;

}
