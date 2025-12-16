package com.cmind.cmind.service;

import com.cmind.cmind.config.PasswordUtil;
import com.cmind.cmind.dto.LoginMember;
import com.cmind.cmind.dto.Member;
import com.cmind.cmind.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordUtil passwordUtil;

    public boolean check_nickname(String nickname) { return memberRepository.existsByNickname(nickname); }

    public boolean check_id(String memberid) { return memberRepository.existsByMemberid(memberid); }

    public void join(Member member) {
        Member member_new = new Member();
        member_new.setNickname(member.getNickname());
        member_new.setMemberid(member.getMemberid());
        member_new.setPassword(passwordUtil.encode(member.getPassword()));

        memberRepository.save(member_new);
    }

    public Member login(LoginMember member) {
        Member findMember = memberRepository.findMemberByMemberid(member.getMemberid());

        if (findMember == null) { return null; }

        if(!passwordUtil.matches(member.getPassword(), findMember.getPassword())) { return null; }

        return findMember;
    }
}
