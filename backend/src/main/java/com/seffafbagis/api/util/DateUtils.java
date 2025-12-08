package com.seffafbagis.api.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date and time utility for Turkish timezone support.
 * 
 * Provides static methods for date/time operations in Istanbul timezone (Europe/Istanbul).
 * All operations default to Istanbul timezone for KVKK compliance and consistency.
 * 
 * Supported operations:
 * - Current date/time in Istanbul timezone
 * - Date formatting and parsing
 * - Date arithmetic (add/subtract days, hours, minutes)
 * - Date range queries (start/end of day)
 * - Expiration checking
 * - Day comparison
 * 
 * Date formats:
 * - DATE_FORMAT: "dd.MM.yyyy" (31.12.2024)
 * - DATETIME_FORMAT: "dd.MM.yyyy HH:mm" (31.12.2024 23:59)
 * 
 * @author Furkan
 * @version 1.0
 */
public final class DateUtils {

    /**
     * Istanbul timezone for Turkey.
     */
    public static final ZoneId ZONE_ISTANBUL = ZoneId.of("Europe/Istanbul");

    /**
     * Date format string: dd.MM.yyyy (31.12.2024).
     * Uses Turkish date format with dot separators.
     */
    public static final String DATE_FORMAT = "dd.MM.yyyy";

    /**
     * DateTime format string: dd.MM.yyyy HH:mm (31.12.2024 23:59).
     * Uses Turkish date and time format.
     */
    public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm";

    /**
     * ISO format for storage and APIs: yyyy-MM-dd.
     */
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * DateTimeFormatter for Turkish date format.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * DateTimeFormatter for Turkish datetime format.
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    /**
     * DateTimeFormatter for ISO date format.
     */
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private DateUtils() {
        throw new AssertionError("Cannot instantiate DateUtils");
    }

    /**
     * Gets current date in Istanbul timezone.
     * 
     * @return Current LocalDate in Istanbul timezone
     */
    public static LocalDate now() {
        return LocalDate.now(ZONE_ISTANBUL);
    }

    /**
     * Gets current date and time in Istanbul timezone.
     * 
     * @return Current LocalDateTime in Istanbul timezone
     */
    public static LocalDateTime nowDateTime() {
        return LocalDateTime.now(ZONE_ISTANBUL);
    }

    /**
     * Gets current instant (UTC).
     * 
     * @return Current Instant
     */
    public static Instant nowInstant() {
        return Instant.now();
    }

    /**
     * Gets current date and time as ZonedDateTime in Istanbul timezone.
     * 
     * @return Current ZonedDateTime in Istanbul timezone
     */
    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now(ZONE_ISTANBUL);
    }

    /**
     * Converts LocalDate to Instant (end of day in Istanbul timezone).
     * 
     * @param date LocalDate to convert
     * @return Instant representing end of day
     */
    public static Instant toInstant(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59).atZone(ZONE_ISTANBUL).toInstant();
    }

    /**
     * Converts LocalDateTime to Instant in Istanbul timezone.
     * 
     * @param dateTime LocalDateTime to convert
     * @return Instant in Istanbul timezone
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZONE_ISTANBUL).toInstant();
    }

    /**
     * Converts Instant to LocalDate in Istanbul timezone.
     * 
     * @param instant Instant to convert
     * @return LocalDate in Istanbul timezone
     */
    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ISTANBUL).toLocalDate();
    }

    /**
     * Converts Instant to LocalDateTime in Istanbul timezone.
     * 
     * @param instant Instant to convert
     * @return LocalDateTime in Istanbul timezone
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ISTANBUL).toLocalDateTime();
    }

    /**
     * Formats LocalDate using Turkish date format (dd.MM.yyyy).
     * 
     * @param date LocalDate to format
     * @return Formatted date string (e.g., "31.12.2024"), or null if input is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats LocalDateTime using Turkish datetime format (dd.MM.yyyy HH:mm).
     * 
     * @param dateTime LocalDateTime to format
     * @return Formatted datetime string (e.g., "31.12.2024 23:59"), or null if input is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Formats Instant using Turkish datetime format in Istanbul timezone.
     * 
     * @param instant Instant to format
     * @return Formatted datetime string, or null if input is null
     */
    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        LocalDateTime dateTime = toLocalDateTime(instant);
        return formatDateTime(dateTime);
    }

    /**
     * Formats LocalDate using ISO format (yyyy-MM-dd).
     * 
     * Suitable for storage and API responses.
     * 
     * @param date LocalDate to format
     * @return ISO formatted date string (e.g., "2024-12-31"), or null if input is null
     */
    public static String formatDateISO(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(ISO_DATE_FORMATTER);
    }

    /**
     * Parses date string in Turkish format (dd.MM.yyyy).
     * 
     * @param dateString Date string to parse (e.g., "31.12.2024")
     * @return Parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses datetime string in Turkish format (dd.MM.yyyy HH:mm).
     * 
     * @param dateTimeString DateTime string to parse (e.g., "31.12.2024 23:59")
     * @return Parsed LocalDateTime, or null if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses date string in ISO format (yyyy-MM-dd).
     * 
     * @param dateString ISO formatted date string (e.g., "2024-12-31")
     * @return Parsed LocalDate, or null if parsing fails
     */
    public static LocalDate parseDateISO(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, ISO_DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if an instant (expiration time) has passed.
     * 
     * Useful for token expiration checking.
     * 
     * @param expirationInstant Expiration time as Instant
     * @return true if expiration time is in the past, false otherwise
     */
    public static boolean isExpired(Instant expirationInstant) {
        if (expirationInstant == null) {
            return false;
        }
        return expirationInstant.isBefore(nowInstant());
    }

    /**
     * Checks if a date is expired (before today).
     * 
     * @param expirationDate Expiration date as LocalDate
     * @return true if expiration date is before today in Istanbul timezone
     */
    public static boolean isExpiredDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(now());
    }

    /**
     * Adds specified number of minutes to an instant.
     * 
     * @param instant Starting instant
     * @param minutes Minutes to add (can be negative)
     * @return New instant with added minutes, or null if input is null
     */
    public static Instant addMinutes(Instant instant, long minutes) {
        if (instant == null) {
            return null;
        }
        return instant.plus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * Adds specified number of hours to an instant.
     * 
     * @param instant Starting instant
     * @param hours Hours to add (can be negative)
     * @return New instant with added hours, or null if input is null
     */
    public static Instant addHours(Instant instant, long hours) {
        if (instant == null) {
            return null;
        }
        return instant.plus(hours, ChronoUnit.HOURS);
    }

    /**
     * Adds specified number of days to an instant.
     * 
     * @param instant Starting instant
     * @param days Days to add (can be negative)
     * @return New instant with added days, or null if input is null
     */
    public static Instant addDays(Instant instant, long days) {
        if (instant == null) {
            return null;
        }
        return instant.plus(days, ChronoUnit.DAYS);
    }

    /**
     * Adds specified number of days to a LocalDate.
     * 
     * @param date Starting date
     * @param days Days to add (can be negative)
     * @return New date with added days, or null if input is null
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plus(days, ChronoUnit.DAYS);
    }

    /**
     * Gets start of day (00:00:00) for a given date in Istanbul timezone.
     * 
     * @param date Date to get start of day for
     * @return Instant representing start of day in Istanbul timezone
     */
    public static Instant startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZONE_ISTANBUL).toInstant();
    }

    /**
     * Gets end of day (23:59:59) for a given date in Istanbul timezone.
     * 
     * @param date Date to get end of day for
     * @return Instant representing end of day in Istanbul timezone
     */
    public static Instant endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59).atZone(ZONE_ISTANBUL).toInstant();
    }

    /**
     * Checks if two instants are on the same day in Istanbul timezone.
     * 
     * @param instant1 First instant
     * @param instant2 Second instant
     * @return true if both instants fall on the same date in Istanbul timezone
     */
    public static boolean isSameDay(Instant instant1, Instant instant2) {
        if (instant1 == null || instant2 == null) {
            return false;
        }
        LocalDate date1 = toLocalDate(instant1);
        LocalDate date2 = toLocalDate(instant2);
        return date1.equals(date2);
    }

    /**
     * Checks if two LocalDates are the same.
     * 
     * @param date1 First date
     * @param date2 Second date
     * @return true if dates are equal
     */
    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }

    /**
     * Calculates days between two instants.
     * 
     * @param startInstant Start instant
     * @param endInstant End instant
     * @return Number of days between (positive if endInstant is after startInstant)
     */
    public static long daysBetween(Instant startInstant, Instant endInstant) {
        if (startInstant == null || endInstant == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startInstant, endInstant);
    }

    /**
     * Calculates days between two LocalDates.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days between (positive if endDate is after startDate)
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculates minutes between two instants.
     * 
     * @param startInstant Start instant
     * @param endInstant End instant
     * @return Number of minutes between (positive if endInstant is after startInstant)
     */
    public static long minutesBetween(Instant startInstant, Instant endInstant) {
        if (startInstant == null || endInstant == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startInstant, endInstant);
    }

    /**
     * Creates a Clock for Istanbul timezone (useful for testing).
     * 
     * @return Clock for Europe/Istanbul timezone
     */
    public static Clock getClock() {
        return Clock.system(ZONE_ISTANBUL);
    }
}
