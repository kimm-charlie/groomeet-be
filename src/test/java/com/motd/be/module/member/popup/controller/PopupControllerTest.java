package com.motd.be.module.member.popup.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.motd.be.BaseIntegrationTest;
import com.motd.be.annotation.ControllerIntegrationTest;
import com.motd.be.module.member.popup.dto.response.PopupFindAllResponse;
import com.motd.be.module.member.popup.dto.response.PopupResponse;
import com.motd.be.module.member.popup.entity.Popup;

@ControllerIntegrationTest
public class PopupControllerTest extends BaseIntegrationTest {

	@Test
	@DisplayName("팝업 전체 조회가 가능하다")
	void findAll() throws Exception {
		// given
		Popup popup1 = popUpProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), Boolean.FALSE, 3);
		//기한이 지나 조회가 안되는 팝업
		Popup popup2 = popUpProvider.save(LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1),
			Boolean.FALSE, 0);
		//삭제되어 조회가 안되는 팝업
		Popup popup3 = popUpProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), Boolean.TRUE, 0);
		Popup popup4 = popUpProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), Boolean.FALSE, 1);
		Popup popup5 = popUpProvider.save(LocalDateTime.now(), LocalDateTime.now().plusDays(1), Boolean.FALSE, 0);

		entityManager.flush();
		entityManager.clear();

		// when
		MvcResult result = mockMvc.perform(get("/api/popups"))
			.andExpect(status().isOk())
			.andReturn();

		// then
		String content = result.getResponse().getContentAsString();
		PopupFindAllResponse response = objectMapper.readValue(content, PopupFindAllResponse.class);

		assertThat(response.getTotalCount()).isEqualTo(3); // 예시 검증
		assertThat(response.getPopUps()).hasSize(3);

		List<Long> returnedIds = response.getPopUps().stream()
			.map(PopupResponse::getId)
			.toList();

		assertThat(returnedIds).containsExactly(popup5.getId(), popup4.getId(), popup1.getId());
	}
}
