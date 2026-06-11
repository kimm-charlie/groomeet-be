package com.motd.be;

import java.net.URI;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api")
@Slf4j
public class RestDocsController {

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/admin/docs/member")
	public ResponseEntity<Resource> getUserApiDocs() {
		Resource resource = new ClassPathResource("static/docs/index-member.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/admin/docs/director")
	public ResponseEntity<Resource> getDirectorApiDocs() {
		Resource resource = new ClassPathResource("static/docs/index-director.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/admin/docs/admin")
	public ResponseEntity<Resource> getAdminApiDocs() {
		Resource resource = new ClassPathResource("static/docs/index-admin.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/admin/docs/exceptions")
	public ResponseEntity<Resource> getErrorApiDocs() {
		Resource resource = new ClassPathResource("static/docs/exception-codes.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@GetMapping("/admin/docs/swagger")
	public ResponseEntity<Void> redirectToSwagger() {
		// 브라우저에서 /api/docs/swagger 요청하면 index.html 로 리다이렉트
		return ResponseEntity.status(HttpStatus.FOUND)
			.location(URI.create("/api/docs/swagger/index.html"))
			.build();
	}

	@GetMapping("/docs/not-found")
	public ResponseEntity<Resource> getErrorPage() {
		Resource resource = new ClassPathResource("static/error/not-found.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	@GetMapping("/docs/unauthorized")
	public ResponseEntity<Resource> getUnauthorizedPage() {
		Resource resource = new ClassPathResource("static/error/unauthorized.html");

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/html");

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

}
