package com.cmind.cmind.repository;

import com.cmind.cmind.dto.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {
    boolean existsByNickname(String nickname);
    boolean existsByMemberid(String memberid);

    Member findMemberByMemberid(String memberid);
}