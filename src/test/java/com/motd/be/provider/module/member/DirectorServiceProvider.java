package com.motd.be.provider.module.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motd.be.module.member.director_service.entity.DirectorService;
import com.motd.be.module.member.director_service.repository.DirectorServiceRepository;

@Component
public class DirectorServiceProvider {

	@Autowired
	private DirectorServiceRepository directorServiceRepository;

	public DirectorService save(String name, DirectorService parent) {
		return directorServiceRepository.save(DirectorService.builder()
			.name(name)
			.parent(parent)
			.isActive(Boolean.TRUE)
			.build());
	}

	public DirectorService saveWithIsActiveFalse(String name, DirectorService parent) {
		return directorServiceRepository.save(DirectorService.builder()
			.name(name)
			.parent(parent)
			.isActive(Boolean.FALSE)
			.build());
	}

	public DirectorService save(String name, DirectorService parent, Integer sortOrder) {
		return directorServiceRepository.save(DirectorService.builder()
			.name(name)
			.parent(parent)
			.sortOrder(sortOrder)
			.isActive(Boolean.TRUE)
			.build());
	}

	public DirectorService save(String name, DirectorService parent, Integer sortOrder, Boolean isActive) {
		return directorServiceRepository.save(DirectorService.builder()
			.name(name)
			.parent(parent)
			.sortOrder(sortOrder)
			.isActive(isActive)
			.isDeleted(false)
			.build());
	}

	public DirectorService saveWithIsDeletedTrue(String name, DirectorService parent, Integer sortOrder) {
		DirectorService directorService = directorServiceRepository.save(DirectorService.builder()
			.name(name)
			.parent(parent)
			.sortOrder(sortOrder)
			.isActive(Boolean.TRUE)
			.isDeleted(false)
			.build());
		directorService.delete();
		return directorServiceRepository.save(directorService);
	}

	public DirectorService findById(Long id) {
		return directorServiceRepository.findById(id).orElseThrow();
	}

	public List<DirectorService> findAll() {
		return directorServiceRepository.findAll();
	}
}
