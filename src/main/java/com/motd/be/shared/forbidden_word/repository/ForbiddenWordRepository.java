package com.motd.be.shared.forbidden_word.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motd.be.shared.forbidden_word.entity.ForbiddenWord;

public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Long> {

	List<ForbiddenWord> findAllByIsActiveTrue();
}
