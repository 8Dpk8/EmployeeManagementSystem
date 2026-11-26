package com.ems.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import com.ems.entity.Employee;
import com.ems.entity.Payroll;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/** Builds the salary slip PDF in memory using iText 5. */
public final class PdfGenerator {

	private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
	private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private static final Font VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, BaseColor.GRAY);
	private static final BaseColor HEADER_BG = new BaseColor(52, 73, 94);

	private PdfGenerator() {
	}

	public static byte[] salarySlip(Payroll payroll) {
		Employee emp = payroll.getEmployee();
		String period = Month.of(payroll.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + payroll.getYear();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4, 40, 40, 40, 40);
		try {
			PdfWriter.getInstance(document, out);
			document.open();

			Paragraph title = new Paragraph("SALARY SLIP", TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph subtitle = new Paragraph("Pay Period: " + period, VALUE_FONT);
			subtitle.setAlignment(Element.ALIGN_CENTER);
			subtitle.setSpacingAfter(20);
			document.add(subtitle);

			PdfPTable details = new PdfPTable(4);
			details.setWidthPercentage(100);
			details.setSpacingAfter(20);
			addHeader(details, "Employee Details", 4);
			addRow(details, "Employee Code", emp.getEmployeeCode(), "Name", emp.getName());
			addRow(details, "Department", emp.getDepartment(), "Designation", emp.getDesignation());
			addRow(details, "Email", emp.getEmail(), "Joining Date", String.valueOf(emp.getJoiningDate()));
			document.add(details);

			PdfPTable salary = new PdfPTable(2);
			salary.setWidthPercentage(100);
			salary.setWidths(new float[] {3, 2});
			addHeader(salary, "Salary Details", 2);
			addAmount(salary, "Basic Salary", payroll.getBasicSalary(), VALUE_FONT);
			addAmount(salary, "Bonus (+)", payroll.getBonus(), VALUE_FONT);
			addAmount(salary, "Deduction (-)", payroll.getDeduction(), VALUE_FONT);
			addAmount(salary, "NET SALARY", payroll.getNetSalary(), LABEL_FONT);
			document.add(salary);

			Paragraph footer = new Paragraph("This is a computer-generated salary slip and does not require a signature. "
					+ "Generated on " + LocalDate.now() + ".", SMALL_FONT);
			footer.setSpacingBefore(30);
			document.add(footer);
		} catch (DocumentException e) {
			throw new IllegalStateException("Could not create salary slip PDF", e);
		} finally {
			document.close();
		}
		return out.toByteArray();
	}

	private static void addHeader(PdfPTable table, String text, int colspan) {
		PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
		cell.setColspan(colspan);
		cell.setBackgroundColor(HEADER_BG);
		cell.setPadding(6);
		table.addCell(cell);
	}

	private static void addRow(PdfPTable table, String label1, String value1, String label2, String value2) {
		table.addCell(cell(label1, LABEL_FONT, Element.ALIGN_LEFT));
		table.addCell(cell(value1, VALUE_FONT, Element.ALIGN_LEFT));
		table.addCell(cell(label2, LABEL_FONT, Element.ALIGN_LEFT));
		table.addCell(cell(value2, VALUE_FONT, Element.ALIGN_LEFT));
	}

	private static void addAmount(PdfPTable table, String label, BigDecimal amount, Font font) {
		table.addCell(cell(label, font, Element.ALIGN_LEFT));
		table.addCell(cell(String.format("%,.2f", amount == null ? BigDecimal.ZERO : amount), font, Element.ALIGN_RIGHT));
	}

	private static PdfPCell cell(String text, Font font, int align) {
		PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
		cell.setHorizontalAlignment(align);
		cell.setPadding(5);
		return cell;
	}
}
