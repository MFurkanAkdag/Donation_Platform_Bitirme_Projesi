package com.seffafbagis.api.util;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilityClassesTest {

    // --- SlugGenerator Tests ---

    @Test
    void testSlugGeneratorBasic() {
        assertEquals("merhaba-dunya", SlugGenerator.generateSlug("Merhaba Dünya"));
    }

    @Test
    void testSlugGeneratorTurkishChars() {
        assertEquals("seffaf-bagis", SlugGenerator.generateSlug("Şeffaf Bağış"));
        assertEquals("igdir-agri", SlugGenerator.generateSlug("Iğdır Ağrı"));
        assertEquals("cankaya", SlugGenerator.generateSlug("Çankaya"));
    }

    @Test
    void testSlugGeneratorSpecialChars() {
        assertEquals("hello-world-2024", SlugGenerator.generateSlug("Hello! World? #2024"));
    }

    @Test
    void testSlugGeneratorMultipleSpaces() {
        assertEquals("multiple-spaces", SlugGenerator.generateSlug("Multiple   Spaces"));
    }

    @Test
    void testSlugGeneratorUnique() {
        List<String> existing = Arrays.asList("test", "test-2");
        String slug = SlugGenerator.generateUniqueSlug("Test", existing::contains);
        assertEquals("test-3", slug);
    }

    // --- ReferenceCodeGenerator Tests ---

    @Test
    void testReferenceCodeGeneratorFormat() {
        String code = ReferenceCodeGenerator.generate();
        assertTrue(ReferenceCodeGenerator.validateFormat(code));
        assertTrue(code.startsWith("SBP-"));
    }

    @Test
    void testReferenceCodeGeneratorDateExtraction() {
        String code = ReferenceCodeGenerator.generate();
        LocalDate extractedDate = ReferenceCodeGenerator.extractDate(code);
        assertEquals(LocalDate.now(), extractedDate);
    }

    // --- ReceiptNumberGenerator Tests ---

    @Test
    void testReceiptNumberGenerator() {
        String receipt = ReceiptNumberGenerator.generate(2024, 42);
        assertEquals("RCPT-2024-000042", receipt);
    }

    @Test
    void testReceiptNumberParsing() {
        String receipt = "RCPT-2024-000042";
        assertEquals(2024, ReceiptNumberGenerator.parseYear(receipt));
        assertEquals(42, ReceiptNumberGenerator.parseSequenceNumber(receipt));
    }

    // --- FileUtils Tests ---

    @Test
    void testFileUtilsExtension() {
        assertEquals("pdf", FileUtils.getFileExtension("file.pdf"));
        assertEquals("pdf", FileUtils.getFileExtension("FILE.PDF"));
        assertEquals("", FileUtils.getFileExtension("noext"));
    }

    @Test
    void testFileUtilsSanitize() {
        assertEquals("Dosya_Adi_1.pdf", FileUtils.sanitizeFilename("Dosya Adı (1).pdf"));
    }

    @Test
    void testFileUtilsSize() {
        assertEquals("1,00 KB", FileUtils.formatFileSize(1024));
        assertEquals("1,00 MB", FileUtils.formatFileSize(1048576));
    }

    // --- StringUtils Tests ---

    @Test
    void testStringUtilsMaskEmail() {
        assertEquals("te***t@example.com", StringUtils.maskEmail("test@example.com"));
        assertEquals("fu***n@gmail.com", StringUtils.maskEmail("furkan@gmail.com"));
    }

    @Test
    void testStringUtilsMaskPhone() {
        // Simple generic mask test
        String masked = StringUtils.maskPhone("+905321234567");
        assertTrue(masked.contains("***"));
    }

    @Test
    void testStringUtilsTitleCase() {
        assertEquals("Merhaba Dünya", StringUtils.toTitleCase("merhaba dünya"));
        assertEquals("İstanbul", StringUtils.toTitleCase("istanbul"));
    }

    // --- NumberUtils Tests ---

    @Test
    void testNumberUtilsCurrency() {
        String formatted = NumberUtils.formatCurrency(new BigDecimal("1234.56"));
        // Depending on environment, might be non-breaking space or space
        assertTrue(formatted.startsWith("1.234,56"));
        assertTrue(formatted.endsWith("TL"));
    }

    @Test
    void testNumberUtilsCompact() {
        assertEquals("1,5 Milyon", NumberUtils.formatCompact(1500000));
        assertEquals("15 Bin", NumberUtils.formatCompact(15000).trim());
    }
}
