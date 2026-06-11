package com.motd.be.module.director.director_info.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
public class DirectorInfoUpdateStoreAddressRequestForDirector {

	private String storeAddress;

}


