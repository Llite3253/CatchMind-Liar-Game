package com.cmind.cmind.controller;

import com.cmind.cmind.config.JwtUtil;
import com.cmind.cmind.dto.LoginMember;
import com.cmind.cmind.dto.Member;
import com.cmind.cmind.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
public class MemberController {
    @Autowired
    MemberService memberService;
    @Autowired
    JwtUtil jwtUtil;

    @GetMapping("/")
    public ModelAndView loginPage() {
        return new ModelAndView("login"); // login.html 렌더링
    }

    @PostMapping("/login/join")
    public ResponseEntity<?> join(@RequestBody Member member) {
        if(!StringUtils.hasText(member.getMemberid()) || !StringUtils.hasText(member.getPassword()) || !StringUtils.hasText(member.getNickname())) {
            return ResponseEntity.badRequest().body("모든 항목을 적어주세요.");
        }

        if(memberService.check_nickname(member.getNickname())) {
            return ResponseEntity.badRequest().body("이미 사용중인 닉네임입니다.");
        }

        if(memberService.check_id(member.getMemberid())) {
            return ResponseEntity.badRequest().body("이미 사용중인 아이디입니다.");
        }

        memberService.join(member);
        return ResponseEntity.ok().body("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginMember member) {
        if(!StringUtils.hasText(member.getMemberid()) || !StringUtils.hasText(member.getPassword())) {
            return ResponseEntity.badRequest().body("모든 항목을 적어주세요.");
        }

        Member login_member = memberService.login(member);
        if(login_member == null) {
            return ResponseEntity.badRequest().body("아이디나 비밀번호가 틀렸습니다.");
        }

        String token = jwtUtil.generateToken(login_member.getMemberid());

        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "token", token,
                "nickname", login_member.getNickname()
        ));
    }
}
