package com.ems.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ems.service.PdfService;

import lombok.RequiredArgsConstructor;

// ADMIN only - enforced by SecurityConfig: .requestMatchers("/download/**").hasRole("ADMIN")
@Controller
@RequestMapping("/download")
@RequiredArgsConstructor
public class DownloadController {

	private final PdfService pdfService;

	/** GET /download/salarySlip/{employeeId}[?month=11&year=2026] -> salary-slip.pdf */
	@GetMapping("/salarySlip/{employeeId}")
	public ResponseEntity<byte[]> salarySlip(@PathVariable Long employeeId,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year) {
		byte[] pdf = pdfService.salarySlip(employeeId, month, year);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename("salary-slip.pdf").build().toString())
				.contentLength(pdf.length)
				.body(pdf);
	}
}
