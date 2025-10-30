package com.clover.bookflow.domain.auth.security.permission;

import com.clover.bookflow.domain.member.entity.Member;
import com.clover.bookflow.domain.readbook.entity.ReadBook;
import com.clover.bookflow.domain.readbook.repository.ReadBookRepository;
import java.io.Serializable;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ReadBookPermissionEvaluator implements DomainPermissionEvaluator {

    private final ReadBookRepository readBookRepository;

    @Override
    public boolean supports(String targetType) {
        return "READ_BOOK".equalsIgnoreCase(targetType);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String permission) {
        if (authentication == null || targetId == null || permission == null) {
            return false;
        }

        Member currentUser = (Member) authentication.getPrincipal();

        Optional<ReadBook> readBookOpt = readBookRepository.findById((Long) targetId);
        if (readBookOpt.isEmpty()) {
            return false;
        }

        ReadBook readBook = readBookOpt.get();

        return switch (permission.toUpperCase()) {
            case "READ" -> true; // 모든 사용자 조회 가능 (필요 시 제한)
            case "WRITE" -> readBook.getMember().equals(currentUser); // 본인만 수정 가능
            case "DELETE" -> readBook.getMember().equals(currentUser); // 본인만 삭제 가능
            default -> false;
        };
    }
}
