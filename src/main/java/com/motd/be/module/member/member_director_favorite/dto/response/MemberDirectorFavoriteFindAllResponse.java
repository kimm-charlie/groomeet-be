package com.motd.be.module.member.member_director_favorite.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.motd.be.module.member.member_director_favorite.entity.MemberDirectorFavorite;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDirectorFavoriteFindAllResponse {

	private int page;
	private Boolean hasNext;
	private List<MemberDirectorFavoriteResponse> directors;

	public static MemberDirectorFavoriteFindAllResponse from(Slice<MemberDirectorFavorite> favorites) {
		return MemberDirectorFavoriteFindAllResponse.builder()
			.page(favorites.getNumber())
			.hasNext(favorites.hasNext())
			.directors(favorites.getContent().stream()
				.map(favorite -> MemberDirectorFavoriteResponse.from(favorite.getTargetMember()))
				.toList())
			.build();
	}
}

