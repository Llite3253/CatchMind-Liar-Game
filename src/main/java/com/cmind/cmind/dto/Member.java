package com.cmind.cmind.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    private String id;
    private String nickname;
    private String memberid;
    private String password;

    public Member(String nickname, String memberid, String password) {
        this.nickname = nickname;
        this.memberid = memberid;
        this.password = password;
    }
}
